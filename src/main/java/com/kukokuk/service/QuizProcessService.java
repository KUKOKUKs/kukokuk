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
        // 1. 세션 저장
        int inserted = quizSessionSummaryMapper.insertQuizSessionSummary(summary);
        if (inserted != 1) throw new RuntimeException("세션 저장 실패");
        int sessionNo = summary.getSessionNo();
        log.info("[DB] 세션 저장 성공 - sessionNo: {}", sessionNo);

        // 2. 퀴즈 결과 저장 및 통계 처리
        for (QuizResult result : results) {
            result.setSessionNo(sessionNo);

            // 정답 정보 조회
            Integer correctChoice = quizMasterMapper.getCorrectChoiceByQuizNo(result.getQuizNo());
            if (correctChoice == null) {
                log.warn("[오류] quizNo={} 에 대한 정답 정보가 존재하지 않음", result.getQuizNo());
                throw new IllegalStateException("정답 정보가 존재하지 않음: quizNo=" + result.getQuizNo());
            }

            // 정답 비교
            String isSuccess = (result.getSelectedChoice() == correctChoice) ? "Y" : "N";
            result.setIsSuccess(isSuccess);

            // 결과 저장
            quizResultMapper.insertQuizResult(result);
            log.info("퀴즈 결과 저장 완료 - quizNo={}, 선택: {}, 정답: {}, 성공여부: {}",
                result.getQuizNo(), result.getSelectedChoice(), correctChoice, isSuccess);

            // 통계 갱신
            quizResultMapper.updateUsageCount(result.getQuizNo());
            if ("Y".equals(isSuccess)) {
                quizResultMapper.updateSuccessCount(result.getQuizNo());
            }
        }

        // 3. 스피드 퀴즈 보충 로직
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
