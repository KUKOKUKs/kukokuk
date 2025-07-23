package com.kukokuk.service;

import com.kukokuk.mapper.QuizResultMapper;
import com.kukokuk.mapper.QuizSessionSummaryMapper;
import com.kukokuk.vo.QuizResult;
import com.kukokuk.vo.QuizSessionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizProcessService {

    private final QuizSessionSummaryMapper quizSessionSummaryMapper;
    private final QuizResultMapper quizResultMapper;

    /**
     * 퀴즈 세션 요약과 문제 결과들을 통합 저장한다.
     * @param summary 세션 요약
     * @param results 문제별 결과 리스트
     */
    @Transactional
    public void insertQuizSessionAndResults(QuizSessionSummary summary, List<QuizResult> results) {
        // 1. 세션 저장 및 sessionNo 자동 주입
        int inserted = quizSessionSummaryMapper.insertQuizSessionSummary(summary);
        if (inserted != 1) {
            throw new RuntimeException("퀴즈 세션 요약 저장 실패");
        }

        int sessionNo = summary.getSessionNo();

        // 2. 퀴즈 결과 저장
        for (QuizResult result : results) {
            result.setSessionNo(sessionNo);
            int resultInserted = quizResultMapper.insertQuizResult(result);
            if (resultInserted != 1) {
                throw new RuntimeException("퀴즈 결과 저장 실패: quizNo=" + result.getQuizNo());
            }

            quizResultMapper.updateUsageCount(result.getQuizNo());
            if ("Y".equals(result.getIsSuccess())) {
                quizResultMapper.updateSuccessCount(result.getQuizNo());
            }
        }
    }
}
