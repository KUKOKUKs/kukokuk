package com.kukokuk.domain.quiz.service;

import com.kukokuk.domain.quiz.mapper.QuizSessionSummaryMapper;
import com.kukokuk.domain.quiz.vo.QuizSessionSummary;
import java.util.List;
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
     */

    public void insertQuizSessionSummary(QuizSessionSummary summary) {
        log.info("[Mapper] insertQuizSessionSummary() summary.quizMode={}", summary.getQuizMode());
        quizSessionSummaryMapper.insertQuizSessionSummary(summary);
    }

    /**
     * 특정 세션 번호 + 유저 번호에 해당하는 퀴즈 요약 정보를 조회한다.
     *
     * @param sessionNo 세션 번호
     * @param userNo    유저 번호
     * @return 퀴즈 요약 정보
     */
    public QuizSessionSummary getSummaryBySessionNoAndUserNo(int sessionNo, int userNo) {
        log.info("QuizSessionSummaryService getSummaryBySessionNoAndUserNo() 실행");
        return quizSessionSummaryMapper.getSummaryBySessionNoAndUserNo(sessionNo, userNo);
    }

    /**
     * 사용자의 특정 컨텐츠타입의 최신 세션 목록 정보 조회
     * @param userNo 사용자 번호
     * @param quizMode 조회할 퀴즈모드
     * @param sessionCount 조회할 개수
     * @return 최근 등록 정렬된 세션 목록 정보
     */
    public List<QuizSessionSummary> getQuizSessionSummaryByUserNoAndMode(
        int userNo, String quizMode, int sessionCount) {
        log.info("QuizSessionSummaryService getQuizSessionSummaryByUserNoAndType() 실행");
        return quizSessionSummaryMapper.getQuizSessionSummaryByUserNoAndMode(userNo, quizMode, sessionCount);
    }

}
