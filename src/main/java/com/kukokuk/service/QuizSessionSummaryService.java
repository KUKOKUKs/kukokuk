package com.kukokuk.service;

import com.kukokuk.mapper.QuizSessionSummaryMapper;
import com.kukokuk.vo.QuizSessionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizSessionSummaryService {

    private final QuizSessionSummaryMapper quizSessionSummaryMapper;

    /**
     * 퀴즈 세션 요약 정보를 저장한다.
     *
     * @param summary 저장할 세션 요약 객체
     * @return 생성된 세션 번호
     */
    @Transactional
    public int insertQuizSessionSummary(QuizSessionSummary summary) {
        int sessionInserted = quizSessionSummaryMapper.insertQuizSessionSummary(summary);
        if (sessionInserted != 1) {
            throw new RuntimeException("퀴즈 세션 저장 실패: userNo=" + summary.getUserNo());
        }

        return summary.getSessionNo(); // useGeneratedKeys를 여기서 사용
    }
}
