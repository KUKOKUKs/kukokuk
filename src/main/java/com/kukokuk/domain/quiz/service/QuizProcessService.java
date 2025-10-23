package com.kukokuk.domain.quiz.service;

import com.kukokuk.common.constant.ContentTypeEnum;
import com.kukokuk.common.constant.DailyQuestEnum;
import com.kukokuk.domain.exp.dto.ExpProcessingDto;
import com.kukokuk.domain.exp.service.ExpProcessingService;
import com.kukokuk.domain.quiz.dto.QuizLevelResultDto;
import com.kukokuk.domain.quiz.mapper.QuizMasterMapper;
import com.kukokuk.domain.quiz.mapper.QuizResultMapper;
import com.kukokuk.domain.quiz.mapper.QuizSessionSummaryMapper;
import com.kukokuk.domain.quiz.vo.QuizResult;
import com.kukokuk.domain.quiz.vo.QuizSessionSummary;
import com.kukokuk.domain.rank.dto.RankProcessingDto;
import com.kukokuk.domain.rank.service.RankService;
import com.kukokuk.domain.user.service.UserService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class QuizProcessService {

    // 스피드 퀴즈 점수 계산 상수
    private static final float MAX_TIME = 300f; // 5분 (300초)
    private static final float MIN_TIME = 30f;   // 30초
    private static final int BASE_SCORE_MULTIPLIER = 100;
    private static final int TIME_BONUS_MULTIPLIER = 5;

    // 퀴즈 풀 유지 상수
    private static final int USAGE_THRESHOLD = 20;
    private static final int TARGET_COUNT = 200;

    // 컨텐츠 타입 상수
    private static final String QUIZ_MODE_SPEED = "speed";
    private static final String QUESTION_TYPE_MEANING = "뜻";
    private static final String QUESTION_TYPE_WORD = "단어";

    private final ExpProcessingService expProcessingService;
    private final UserService userService;
    private final QuizSessionSummaryMapper quizSessionSummaryMapper;
    private final QuizResultMapper quizResultMapper;
    private final QuizMasterMapper quizMasterMapper;
    private final QuizService quizService;
    private final QuizSessionSummaryService quizSessionSummaryService;
    private final RankService rankService;

    /**
     * 퀴즈 세션 요약과 결과 저장 + 퀴즈 자동 보충 처리
     *
     * @param summary 세션 요약
     * @param results 문제 결과 리스트
     * @return 생성된 sessionNo
     */
    @Transactional
    public int insertQuizSessionAndResults(QuizSessionSummary summary, List<QuizResult> results) {
        log.info("[Service] insertQuizSessionAndResults() summary.quizMode={}", summary.getQuizMode());
        log.info("[시작] insertQuizSessionAndResults() - userNo={}, 문제 수={}", summary.getUserNo(), results.size());

        int totalQuestion = results.size();
        int correctAnswers = 0;

        initializeQuizSessionSummary(summary, totalQuestion);

        // 세션 insert (sessionNo 채워짐)
        quizSessionSummaryService.insertQuizSessionSummary(summary);
        int sessionNo = summary.getSessionNo();
        log.info("[세션 저장 완료] sessionNo={}", sessionNo);

        // 퀴즈 결과 처리
        correctAnswers = processQuizResults(results, sessionNo);

        // 세션 요약 갱신
        updateQuizSessionSummary(summary, totalQuestion, correctAnswers);

        // 경험치 처리
        processExperiencePoints(summary, correctAnswers, sessionNo);

        log.info("[전체 처리 완료] 세션 {}, 정답 수: {}, 상위 {}%에 속함",
            sessionNo, correctAnswers, summary.getPercentile());

        // 스피드퀴즈 점수 계산 및 월별 랭킹 처리
        if (QUIZ_MODE_SPEED.equals(summary.getQuizMode())) {
            processSpeedQuizMonthlyRanking(summary, correctAnswers, totalQuestion);
        }

        // 스피드 퀴즈 풀 유지
        maintainSpeedQuizPool();

        return sessionNo;
    }

    /**
     * 퀴즈 세션 요약 정보를 초기화한다.
     *
     * @param summary 초기화할 세션 요약 객체
     * @param totalQuestion 전체 문제 수
     */
    private void initializeQuizSessionSummary(QuizSessionSummary summary, int totalQuestion) {
        summary.setTotalQuestion(totalQuestion);
        summary.setCorrectAnswers(0);
        summary.setAverageTimePerQuestion(
            totalQuestion == 0 ? 0f : summary.getTotalTimeSec() / totalQuestion);
        summary.setPercentile(0);
    }

    /**
     * 퀴즈 결과를 처리하고 통계를 업데이트한다.
     * 각 문제별 정답 여부를 확인하고, 결과를 저장하며, 퀴즈 통계를 갱신한다.
     *
     * @param results 퀴즈 결과 리스트
     * @param sessionNo 세션 번호
     * @return 정답 개수
     */
    private int processQuizResults(List<QuizResult> results, int sessionNo) {
        int correctAnswers = 0;

        for (QuizResult result : results) {
            result.setSessionNo(sessionNo);

            Integer correctChoice = quizMasterMapper.getCorrectChoiceByQuizNo(result.getQuizNo());
            if (correctChoice == null) {
                log.error("[정답 없음] quizNo={}", result.getQuizNo());
                throw new IllegalStateException("정답 정보 없음: quizNo=" + result.getQuizNo());
            }

            boolean isCorrect = result.getSelectedChoice() == correctChoice;
            result.setIsSuccess(isCorrect ? "Y" : "N");
            if (isCorrect) {
                correctAnswers++;
            }

            // 결과 저장 및 통계 업데이트
            quizResultMapper.insertQuizResult(result);
            updateQuizStatistics(result.getQuizNo(), isCorrect);
        }

        return correctAnswers;
    }

    /**
     * 퀴즈 통계를 업데이트한다.
     * 풀이 횟수, 정답 횟수를 증가시키고, 20회 도달 시 정답률과 난이도를 자동 산정한다.
     *
     * @param quizNo 퀴즈 번호
     * @param isCorrect 정답 여부
     */
    private void updateQuizStatistics(int quizNo, boolean isCorrect) {
        // 정답률/난이도는 20회 도달 순간(19→20)에서만 1회 확정
        quizResultMapper.updateUsageCount(quizNo);
        if (isCorrect) {
            quizResultMapper.updateSuccessCount(quizNo);
        }

        // update 후 최신 usageCount 조회
        int afterUsage = quizMasterMapper.getUsageCount(quizNo);

        // 처음 20회 도달 시에만 정확도/난이도 산정
        if (afterUsage == USAGE_THRESHOLD) {
            quizResultMapper.updateAccuracyRate(quizNo);
            quizResultMapper.updateDifficulty(quizNo);
        }
    }

    /**
     * 퀴즈 세션 요약 정보를 업데이트한다.
     * 정답 수, 평균 풀이 시간, 퍼센타일을 계산하여 갱신한다.
     *
     * @param summary 세션 요약 객체
     * @param totalQuestion 전체 문제 수
     * @param correctAnswers 정답 수
     */
    private void updateQuizSessionSummary(QuizSessionSummary summary, int totalQuestion, int correctAnswers) {
        summary.setCorrectAnswers(correctAnswers);
        summary.setAverageTimePerQuestion(
            totalQuestion == 0 ? 0f : summary.getTotalTimeSec() / totalQuestion);

        // 퍼센타일 계산
        int percentile = calculatePercentile(summary);
        summary.setPercentile(percentile);

        quizSessionSummaryMapper.updateQuizSessionSummary(summary);
    }

    /**
     * 백분율을 계산한다.
     * 현재 세션보다 성적이 좋은 세션의 비율을 백분율로 계산한다.
     *
     * @param summary 세션 요약 객체
     * @return 퍼센티지 (1-100)
     */
    private int calculatePercentile(QuizSessionSummary summary) {
        int betterCount = quizSessionSummaryMapper.getCountBetterSessions(
            summary.getCorrectAnswers(),
            summary.getAverageTimePerQuestion()
        );
        int totalCount = quizSessionSummaryMapper.getTotalSessionCount();

        int percentile = 0;
        if (totalCount > 0) {
            float ratio = (betterCount / (float) totalCount) * 100;
            percentile = Math.round(ratio);
            percentile = Math.max(1, percentile); // 최소 1% 보정
        }

        return percentile;
    }

    /**
     * 스피드 퀴즈 유지: 각 유형별 퀴즈 수가 200개 미만이면 자동 보충
     */
    private void maintainSpeedQuizPool() {
        maintainQuizPoolByType(QUESTION_TYPE_MEANING, () -> {
            int currentCount = quizMasterMapper.getQuizCountByTypeAndUsageCount(QUESTION_TYPE_MEANING, USAGE_THRESHOLD);
            if (currentCount < TARGET_COUNT) {
                int toCreate = TARGET_COUNT - currentCount;
                quizService.insertQuizByWordRandomEntry(toCreate);
                log.info("보충된 뜻 유형 퀴즈 {}개", toCreate);
            }
        });

        maintainQuizPoolByType(QUESTION_TYPE_WORD, () -> {
            int currentCount = quizMasterMapper.getQuizCountByTypeAndUsageCount(QUESTION_TYPE_WORD, USAGE_THRESHOLD);
            if (currentCount < TARGET_COUNT) {
                int toCreate = TARGET_COUNT - currentCount;
                quizService.insertQuizByDefRandomEntry(toCreate);
                log.info("보충된 단어 유형 퀴즈 {}개", toCreate);
            }
        });
    }

    /**
     * 특정 타입의 퀴즈 풀을 유지한다.
     *
     * @param questionType 문제 유형
     * @param maintainAction 유지 작업을 수행하는 Runnable
     */
    private void maintainQuizPoolByType(String questionType, Runnable maintainAction) {
        maintainAction.run();
    }

    /**
     * 퀴즈 완료 시 경험치를 처리한다.
     * 퀴즈 모드와 정답 수에 따라 경험치를 계산하고, 경험치 처리 서비스를 호출한다.
     *
     * @param summary 세션 요약 정보
     * @param correctAnswers 정답 수
     * @param sessionNo 세션 번호
     */
    private void processExperiencePoints(QuizSessionSummary summary, int correctAnswers, int sessionNo) {
        String quizMode = summary.getQuizMode();
        int userNo = summary.getUserNo();

        // 경험치 계산
        int expGained = calculateExperiencePoints(quizMode, correctAnswers, sessionNo);

        if (expGained > 0) {
            ExpProcessingDto expProcessingDto = ExpProcessingDto.builder()
                .userNo(userNo)
                .contentType(quizMode.equals("speed")
                    ? ContentTypeEnum.SPEED.name()
                    : ContentTypeEnum.LEVEL.name())
                .contentNo(sessionNo)
                .expGained(expGained)
                .dailyQuestNo(quizMode.equals("speed")
                    ? DailyQuestEnum.QUIZ_SPEED.getDailyQuestNo()
                    : DailyQuestEnum.QUIZ_LEVEL.getDailyQuestNo())
                .build();

            // 경험치 처리 서비스 호출
            expProcessingService.expProcessing(expProcessingDto);

            log.info("[경험치 처리 완료] userNo={}, mode={}, exp={}", userNo, quizMode, expGained);
        }
    }

    /**
     * 퀴즈 모드별 경험치를 계산한다.
     *
     * @param quizMode 퀴즈 모드 ("speed" 또는 "level")
     * @param correctAnswers 정답 수
     * @param sessionNo 세션 번호
     * @return 획득 경험치
     */
    private int calculateExperiencePoints(String quizMode, int correctAnswers, int sessionNo) {
        if ("speed".equals(quizMode)) {
            // 스피드퀴즈: 정답 1개당 3exp
            return correctAnswers * 3;
        } else if ("level".equals(quizMode)) {
            // 단계별퀴즈: 난이도별 차등 지급
            return calculateLevelQuizExperience(correctAnswers, sessionNo);
        }
        return 0;
    }

    /**
     * 단계별 퀴즈의 난이도별 경험치를 계산한다.
     * 난이도에 따라 문제당 차등 경험치를 지급한다.
     * - 쉬움: 2exp per question (최대 20exp)
     * - 보통: 3exp per question (최대 30exp)
     * - 어려움: 4exp per question (최대 40exp)
     *
     * @param correctAnswers 정답 수
     * @param sessionNo 세션 번호
     * @return 획득 경험치
     */
    private int calculateLevelQuizExperience(int correctAnswers, int sessionNo) {
        // 세션에서 난이도 정보 조회
        QuizLevelResultDto levelResult = quizMasterMapper.getDifficultyAndQuestionTypeBySessionNo(sessionNo);

        if (levelResult == null || levelResult.getDifficulty() == null) {
            log.warn("[경험치 계산] 난이도 정보 없음 - sessionNo: {}", sessionNo);
            return correctAnswers * 3; // 기본값
        }

        String difficulty = levelResult.getDifficulty();
        int expPerQuestion = switch (difficulty) {
            case "쉬움" -> 2;      // 쉬움: 2exp (최대 20exp)
            case "보통" -> 3;      // 보통: 3exp (최대 30exp)
            case "어려움" -> 4;    // 어려움: 4exp (최대 40exp)
            default -> {
                log.warn("[경험치 계산] 알 수 없는 난이도: {}", difficulty);
                yield 3;   // 기본값
            }
        };

        log.info("[단계별퀴즈 경험치] 난이도: {}, 정답수: {}, 문제당: {}exp, 총: {}exp",
            difficulty, correctAnswers, expPerQuestion, correctAnswers * expPerQuestion);

        return correctAnswers * expPerQuestion;
    }

    /**
     * 스피드퀴즈 점수 계산 및 월별 랭킹 처리
     * @param summary 퀴즈 세션 요약
     * @param correctAnswers 정답 수
     * @param totalQuestion 전체 문제 수
     */
    private void processSpeedQuizMonthlyRanking(QuizSessionSummary summary, int correctAnswers, int totalQuestion) {
        log.info("[월별 랭킹 처리 시작] 사용자: {}, 정답: {}/{}", summary.getUserNo(), correctAnswers, totalQuestion);

        double finalScore = calculateSpeedQuizScore(summary, correctAnswers, totalQuestion);

        // 월별 랭킹 처리 (현재 월 기준으로 신규 등록 또는 평균 업데이트)
        try {
            rankService.rankProcessing(
                RankProcessingDto.builder()
                    .userNo(summary.getUserNo())
                    .contentType(ContentTypeEnum.SPEED.name())
                    .score(BigDecimal.valueOf(finalScore))
                    .build()
            );
            log.info("[월별 랭킹 처리 완료] 사용자: {}, 점수: {:.2f}", summary.getUserNo(), finalScore);
        } catch (Exception e) {
            log.error("[월별 랭킹 처리 실패] 사용자: {}, 점수: {:.2f}", summary.getUserNo(), finalScore, e);
            // 랭킹 실패가 퀴즈 처리 전체를 망치지 않도록 예외를 잡아서 로그만 남김
        }
    }

    /**
     * 스피드퀴즈 점수 계산
     * BASE_SCORE: (정답수 ÷ 전체문제수) × 100
     * TIME_BONUS: ((MAX_TIME - 실제시간) ÷ (MAX_TIME - MIN_TIME)) × 5
     * TOTAL_SCORE: BASE_SCORE + TIME_BONUS
     */
    private double calculateSpeedQuizScore(QuizSessionSummary summary, int correctAnswers, int totalQuestion) {
        // BASE_SCORE: (정답수 ÷ 전체문제수) × 100
        double baseScore = ((double) correctAnswers / totalQuestion) * BASE_SCORE_MULTIPLIER;

        // TIME_BONUS: ((MAX_TIME - 실제시간) ÷ (MAX_TIME - MIN_TIME)) × 5
        float actualTime = Math.max(MIN_TIME, Math.min(MAX_TIME, summary.getTotalTimeSec()));
        double timeBonus = ((MAX_TIME - actualTime) / (MAX_TIME - MIN_TIME)) * TIME_BONUS_MULTIPLIER;

        // TOTAL_SCORE: BASE_SCORE + TIME_BONUS
        double finalScore = baseScore + timeBonus;

        log.info("[점수 계산] 기본점수: {:.2f}, 시간보너스: {:.2f}, 최종점수: {:.2f}",
            baseScore, timeBonus, finalScore);

        return finalScore;
    }
}