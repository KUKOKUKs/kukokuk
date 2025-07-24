package com.kukokuk.service;

import com.kukokuk.mapper.QuizResultMapper;
import com.kukokuk.mapper.QuizSessionSummaryMapper;
import com.kukokuk.mapper.QuizMasterMapper;
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
    private final QuizMasterMapper quizMasterMapper;
    private final QuizService quizService;

    /**
     * 퀴즈 세션 요약과 결과 저장 + 퀴즈 자동 보충 처리
     * @param summary 세션 요약
     * @param results 문제 결과 리스트
     */
    @Transactional
    public void insertQuizSessionAndResults(QuizSessionSummary summary, List<QuizResult> results) {
        // 1. 세션 저장
        int inserted = quizSessionSummaryMapper.insertQuizSessionSummary(summary);
        if (inserted != 1) throw new RuntimeException("세션 저장 실패");

        int sessionNo = summary.getSessionNo();

        // 2. 퀴즈 결과 저장
        for (QuizResult result : results) {
            result.setSessionNo(sessionNo);
            quizResultMapper.insertQuizResult(result);
            quizResultMapper.updateUsageCount(result.getQuizNo());

            if ("Y".equals(result.getIsSuccess())) {
                quizResultMapper.updateSuccessCount(result.getQuizNo());
            }
        }

        // 3. 세션 종료 후 퀴즈 자동 보충
        maintainSpeedQuizPool();
    }

    /**
     * 스피드 퀴즈 유지: 각 유형별 퀴즈 수가 100개 미만이면 자동 보충
     */
    private void maintainSpeedQuizPool() {
        final int USAGE_THRESHOLD = 20;
        final int TARGET_COUNT = 100;

        // 뜻 유형
        int currentMeaning = quizMasterMapper.getQuizCountByTypeAndUsageCount("뜻", USAGE_THRESHOLD);
        if (currentMeaning < TARGET_COUNT) {
            int toCreate = TARGET_COUNT - currentMeaning;
            quizService.insertQuizByWordRandomEntry(toCreate);
            System.out.println("뜻 퀴즈 " + toCreate + "개 보충 생성");
        }

        // 단어 유형
        int currentWord = quizMasterMapper.getQuizCountByTypeAndUsageCount("단어", USAGE_THRESHOLD);
        if (currentWord < TARGET_COUNT) {
            int toCreate = TARGET_COUNT - currentWord;
            quizService.insertQuizByDefRandomEntry(toCreate);
            System.out.println("단어 퀴즈 " + toCreate + "개 보충 생성");
        }
    }
}
