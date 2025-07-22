package com.kukokuk.service;

import com.kukokuk.mapper.QuizResultMapper;
import com.kukokuk.vo.QuizResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizResultService {

    private final QuizResultMapper quizResultMapper;

    /**
     * 퀴즈 결과 저장 및 문제 통계 업데이트
     * @param result 퀴즈 결과
     */
    @Transactional
    public void saveQuizResult(QuizResult result) {
        // 1) 퀴즈 결과 저장
        int insertedRows = quizResultMapper.insertQuizResult(result);

        // 2) 문제 풀이 횟수 업데이트
        int usageUpdated = quizResultMapper.updateUsageCount(result.getQuizNo());

        // 3) 정답인 경우 성공 횟수 업데이트
        if ("Y".equals(result.getIsSuccess())) {
            int successUpdated = quizResultMapper.updateSuccessCount(result.getQuizNo());
        }
    }
}
