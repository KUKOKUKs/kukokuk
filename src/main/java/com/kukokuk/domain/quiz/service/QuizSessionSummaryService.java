package com.kukokuk.domain.quiz.service;

import com.kukokuk.domain.quiz.mapper.QuizSessionSummaryMapper;
import com.kukokuk.domain.quiz.vo.QuizSessionSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class QuizSessionSummaryService {

    private final QuizSessionSummaryMapper quizSessionSummaryMapper;

    /**
     * 퀴즈 세션 요약 정보를 저장한다.
     *
     * @param summary 저장할 세션 요약 객체
     * @return 생성된 세션 번호
     */

    public int insertQuizSessionSummary(QuizSessionSummary summary) {
        log.info("[Mapper] insertQuizSessionSummary() summary.quizMode={}", summary.getQuizMode());
        quizSessionSummaryMapper.insertQuizSessionSummary(summary);

        return summary.getSessionNo();
    }

    /**
     * 특정 세션 번호 + 유저 번호에 해당하는 퀴즈 요약 정보를 조회한다.
     *
     * @param sessionNo 세션 번호
     * @param userNo    유저 번호
     * @return 퀴즈 요약 정보
     */
    public QuizSessionSummary getSummaryBySessionNoAndUserNo(int sessionNo, int userNo) {
        QuizSessionSummary summary = quizSessionSummaryMapper.getSummaryBySessionNoAndUserNo(
            sessionNo, userNo);
        if (summary == null) {
            throw new RuntimeException("해당 세션 정보를 찾을 수 없습니다. sessionNo=" + sessionNo);
        }
        return summary;
    }
}
