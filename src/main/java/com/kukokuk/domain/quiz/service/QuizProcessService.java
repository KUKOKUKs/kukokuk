package com.kukokuk.domain.quiz.service;

import com.kukokuk.domain.quiz.mapper.QuizMasterMapper;
import com.kukokuk.domain.quiz.mapper.QuizResultMapper;
import com.kukokuk.domain.quiz.mapper.QuizSessionSummaryMapper;
import com.kukokuk.domain.quiz.vo.QuizResult;
import com.kukokuk.domain.quiz.vo.QuizSessionSummary;
import com.kukokuk.domain.ranking.service.RankingService;
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
    private static final String CONTENT_TYPE_SPEED = "SPEED";
    private static final String QUIZ_MODE_SPEED = "speed";
    private static final String QUESTION_TYPE_MEANING = "뜻";
    private static final String QUESTION_TYPE_WORD = "단어";

    private final QuizSessionSummaryMapper quizSessionSummaryMapper;
    private final QuizResultMapper quizResultMapper;
    private final QuizMasterMapper quizMasterMapper;
    private final QuizService quizService;
    private final QuizSessionSummaryService quizSessionSummaryService;
    private final RankingService rankingService;

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
     * 퀴즈 세션 요약 초기화
     */
    private void initializeQuizSessionSummary(QuizSessionSummary summary, int totalQuestion) {
        summary.setTotalQuestion(totalQuestion);
        summary.setCorrectAnswers(0);
        summary.setAverageTimePerQuestion(
            totalQuestion == 0 ? 0f : summary.getTotalTimeSec() / totalQuestion);
        summary.setPercentile(0);
    }

    /**
     * 퀴즈 결과 처리
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
     * 퀴즈 통계 업데이트
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
     * 퀴즈 세션 요약 업데이트
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
     * 퍼센타일 계산
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
     * 특정 타입의 퀴즈 풀 유지
     */
    private void maintainQuizPoolByType(String questionType, Runnable maintainAction) {
        maintainAction.run();
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
            rankingService.processMonthlyRanking(CONTENT_TYPE_SPEED, finalScore, summary.getUserNo());
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

    /**
     * @deprecated 월별 랭킹으로 변경됨. processSpeedQuizMonthlyRanking() 사용 권장
     */
    @Deprecated
    private void processSpeedQuizRanking(QuizSessionSummary summary, int correctAnswers, int totalQuestion) {
        log.warn("Deprecated processSpeedQuizRanking() 호출됨. processSpeedQuizMonthlyRanking() 사용 권장");
        processSpeedQuizMonthlyRanking(summary, correctAnswers, totalQuestion);
    }
}