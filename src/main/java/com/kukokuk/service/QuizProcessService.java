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

    /**
     * 퀴즈 세션 요약과 결과 저장 + 퀴즈 자동 보충 처리
     * @param summary 세션 요약
     * @param results 문제 결과 리스트
     */
    @Transactional
    public void insertQuizSessionAndResults(QuizSessionSummary summary, List<QuizResult> results) {
        log.info("[시작] insertQuizSessionAndResults() - userNo={}, 문제 수={}", summary.getUserNo(), results.size());

        // 1. 세션 저장
        int inserted = quizSessionSummaryMapper.insertQuizSessionSummary(summary);
        log.info("[확인용] insert 결과 inserted = {}, sessionNo = {}", inserted, summary.getSessionNo());
        if (inserted != 1) throw new RuntimeException("세션 저장 실패");

        int sessionNo = summary.getSessionNo();
        log.info("[DB 저장 완료] 퀴즈 세션 저장 성공 - sessionNo: {}", sessionNo);

        for (QuizResult result : results) {
            result.setSessionNo(sessionNo);

            Integer correctChoice = quizMasterMapper.getCorrectChoiceByQuizNo(result.getQuizNo());
            if (correctChoice == null) {
                log.error("[오류] 정답 정보 없음 - quizNo={}", result.getQuizNo());
                throw new IllegalStateException("정답 정보가 존재하지 않음: quizNo=" + result.getQuizNo());
            }

            // 정답 여부 저장
            boolean isCorrect = result.getSelectedChoice() == correctChoice;
            result.setIsSuccess(isCorrect ? "Y" : "N");

            // 결과 저장
            quizResultMapper.insertQuizResult(result);
            log.info("[결과 저장] quizNo={}, 선택={}, 정답={}, 성공여부={}",
                result.getQuizNo(), result.getSelectedChoice(), correctChoice, result.getIsSuccess());

            // 퀴즈 통계 업데이트
            quizResultMapper.updateUsageCount(result.getQuizNo());
            if ("Y".equals(result.getIsSuccess())) {
                quizResultMapper.updateSuccessCount(result.getQuizNo());
            }
        }

        log.info("[완료] 전체 퀴즈 결과 처리 완료 - sessionNo={}", sessionNo);

        // 3. 스피드 퀴즈 보충
        maintainSpeedQuizPool();
    }

    /**
     * 스피드 퀴즈 유지: 각 유형별 퀴즈 수가 100개 미만이면 자동 보충
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
