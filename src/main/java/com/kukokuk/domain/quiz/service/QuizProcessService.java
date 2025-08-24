package com.kukokuk.domain.quiz.service;

import com.kukokuk.domain.quiz.mapper.QuizMasterMapper;
import com.kukokuk.domain.quiz.mapper.QuizResultMapper;
import com.kukokuk.domain.quiz.mapper.QuizSessionSummaryMapper;
import com.kukokuk.domain.quiz.vo.QuizResult;
import com.kukokuk.domain.quiz.vo.QuizSessionSummary;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class QuizProcessService {

    private final QuizSessionSummaryMapper quizSessionSummaryMapper;
    private final QuizResultMapper quizResultMapper;
    private final QuizMasterMapper quizMasterMapper;
    private final QuizService quizService;
    private final QuizSessionSummaryService quizSessionSummaryService;

    /**
     * 퀴즈 세션 요약과 결과 저장 + 퀴즈 자동 보충 처리
     *
     * @param summary 세션 요약
     * @param results 문제 결과 리스트
     * @return 생성된 sessionNo
     */
    @Transactional
    public int insertQuizSessionAndResults(QuizSessionSummary summary, List<QuizResult> results) {
        log.info("[Service] insertQuizSessionAndResults() summary.quizMode={}",
            summary.getQuizMode());
        log.info("[시작] insertQuizSessionAndResults() - userNo={}, 문제 수={}", summary.getUserNo(),
            results.size());

        int totalQuestion = results.size();
        int correctAnswers = 0;

        summary.setTotalQuestion(totalQuestion);
        summary.setCorrectAnswers(0);
        summary.setAverageTimePerQuestion(
            totalQuestion == 0 ? 0f : summary.getTotalTimeSec() / totalQuestion);
        summary.setPercentile(0);

        // 세션 insert (sessionNo 채워짐)
        quizSessionSummaryService.insertQuizSessionSummary(summary);
        int sessionNo = summary.getSessionNo();
        log.info("[세션 저장 완료] sessionNo={}", sessionNo);

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

            // 결과 저장
            quizResultMapper.insertQuizResult(result);

            // 정답률/난이도는 20회 도달 순간(19→20)에서만 1회 확정
            quizResultMapper.updateUsageCount(result.getQuizNo());
            if (isCorrect) {
                quizResultMapper.updateSuccessCount(result.getQuizNo());
            }

            // update 후 최신 usageCount 조회
            int afterUsage = quizMasterMapper.getUsageCount(result.getQuizNo());

            // 처음 20회 도달 시에만 정확도/난이도 산정
            if (afterUsage == 20) {
                quizResultMapper.updateAccuracyRate(result.getQuizNo());
                quizResultMapper.updateDifficulty(result.getQuizNo());
            }
        }

        // 세션 요약 갱신
        summary.setCorrectAnswers(correctAnswers);
        summary.setAverageTimePerQuestion(
            totalQuestion == 0 ? 0f : summary.getTotalTimeSec() / totalQuestion);

        // 퍼센타일 계산 방식 (전체 기준)
        int betterCount = quizSessionSummaryMapper.getCountBetterSessions(
            summary.getCorrectAnswers(),
            summary.getAverageTimePerQuestion()
        );
        int totalCount = quizSessionSummaryMapper.getTotalSessionCount();

        int percentile = (totalCount == 0) ? 0
            : (int)(((totalCount - betterCount) / (float) totalCount) * 100);

        summary.setPercentile(percentile);
        quizSessionSummaryMapper.updateQuizSessionSummary(summary);

        log.info("[전체 처리 완료] 세션 {}, 정답 수: {}, 상위 퍼센트: {}", sessionNo, correctAnswers, percentile);

        // 스피드 퀴즈 풀 유지
        maintainSpeedQuizPool();

        return sessionNo;
    }

    /**
     * 스피드 퀴즈 유지: 각 유형별 퀴즈 수가 200개 미만이면 자동 보충
     */
    private void maintainSpeedQuizPool() {
        final int USAGE_THRESHOLD = 20;
        final int TARGET_COUNT = 200;

        int currentMeaning = quizMasterMapper.getQuizCountByTypeAndUsageCount("뜻", USAGE_THRESHOLD);
        if (currentMeaning < TARGET_COUNT) {
            int toCreate = TARGET_COUNT - currentMeaning;
            quizService.insertQuizByWordRandomEntry(toCreate);
            log.info("보충된 뜻 유형 퀴즈 {}개", toCreate);
        }

        int currentWord = quizMasterMapper.getQuizCountByTypeAndUsageCount("단어", USAGE_THRESHOLD);
        if (currentWord < TARGET_COUNT) {
            int toCreate = TARGET_COUNT - currentWord;
            quizService.insertQuizByDefRandomEntry(toCreate);
            log.info("보충된 단어 유형 퀴즈 {}개", toCreate);
        }
    }
}
