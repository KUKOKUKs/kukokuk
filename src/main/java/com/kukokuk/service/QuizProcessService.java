package com.kukokuk.service;

import com.kukokuk.mapper.QuizMasterMapper;
import com.kukokuk.mapper.QuizResultMapper;
import com.kukokuk.mapper.QuizSessionSummaryMapper;
import com.kukokuk.vo.QuizResult;
import com.kukokuk.vo.QuizSessionSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * @param summary 세션 요약
     * @param results 문제 결과 리스트
     * @return 생성된 sessionNo
     */
    @Transactional
    public int insertQuizSessionAndResults(QuizSessionSummary summary, List<QuizResult> results) {
        log.info("[시작] insertQuizSessionAndResults() - userNo={}, 문제 수={}", summary.getUserNo(), results.size());

        // [1] 세션 요약 필드 계산 및 설정
        int totalQuestion = results.size();
        int correctAnswers = 0;

        summary.setTotalQuestion(totalQuestion);
        summary.setCorrectAnswers(0);  // 초기값
        summary.setAverageTimePerQuestion(totalQuestion == 0 ? 0f : summary.getTotalTimeSec() / totalQuestion);
        summary.setPercentile(0); // 추후 랭킹 계산용
        summary.setQuizMode("speed");

        log.info("[summary 설정 완료] {}", summary);

        // [2] 세션 저장
        quizSessionSummaryService.insertQuizSessionSummary(summary);

        int sessionNo = summary.getSessionNo(); // keyProperty 로 전달된 sessionNo
        log.info("[세션 저장 완료] sessionNo={}", sessionNo);

        // [3] 결과 저장 및 통계 처리
        for (QuizResult result : results) {
            result.setSessionNo(sessionNo);

            Integer correctChoice = quizMasterMapper.getCorrectChoiceByQuizNo(result.getQuizNo());
            if (correctChoice == null) {
                log.error("[정답 없음] quizNo={}", result.getQuizNo());
                throw new IllegalStateException("정답 정보 없음: quizNo=" + result.getQuizNo());
            }

            boolean isCorrect = result.getSelectedChoice() == correctChoice;
            result.setIsSuccess(isCorrect ? "Y" : "N");
            if (isCorrect) correctAnswers++;

            quizResultMapper.insertQuizResult(result);
            quizResultMapper.updateUsageCount(result.getQuizNo());
            if (isCorrect) {
                quizResultMapper.updateSuccessCount(result.getQuizNo());
            }

            int usageCount = quizMasterMapper.getUsageCount(result.getQuizNo());
            if (usageCount == 20) {
                quizResultMapper.updateAccuracyRate(result.getQuizNo());
                quizResultMapper.updateDifficulty(result.getQuizNo());
            }
        }


        // [4] 세션 요약 정답 수 갱신
        summary.setCorrectAnswers(correctAnswers);
        summary.setAverageTimePerQuestion(totalQuestion == 0 ? 0f : summary.getTotalTimeSec() / totalQuestion);

        // 상위 퍼센트 계산
        int sameGroupTotal = quizSessionSummaryMapper.getCountSameSessions(correctAnswers);
        int slowerCount = quizSessionSummaryMapper.getCountSlowerSessions(correctAnswers, summary.getAverageTimePerQuestion());

        int percentile = (sameGroupTotal == 0) ? 0 :
            (int) (((sameGroupTotal - slowerCount) / (float) sameGroupTotal) * 100);

        summary.setPercentile(percentile);
        quizSessionSummaryMapper.updateQuizSessionSummary(summary); // 최종 update

        log.info("[전체 처리 완료] 세션 {}, 정답 수: {}, 상위 퍼센트: {}",
            sessionNo, correctAnswers, percentile);

        // [5] 퀴즈 자동 보충
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
