package com.kukokuk.mapper;

import com.kukokuk.vo.QuizSessionSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 퀴즈 세션 요약 정보를 처리하는 MyBatis 매퍼
 */
@Mapper
public interface QuizSessionSummaryMapper {

    /**
     * 퀴즈 세션 요약 정보를 저장한다.
     *
     * @param summary 저장할 퀴즈 세션 요약 객체
     * @return insert된 행 수 (성공 시 1)
     */
    int insertQuizSessionSummary(QuizSessionSummary summary);

    /**
     * 퀴즈 세션 요약 정보를 수정한다. 총 정답 수, 평균 시간 등을 세션 완료 후 갱신할 때 사용한다.
     *
     * @param summary 수정할 퀴즈 세션 요약 객체
     * @return 수정된 행 수 (성공 시 1)
     */
    int updateQuizSessionSummary(QuizSessionSummary summary);

    /**
     * 세션 번호와 유저 번호로 퀴즈 세션 요약 정보를 조회한다. 결과 화면에 표시할 요약 정보 조회 시 사용.
     *
     * @param sessionNo 세션 번호
     * @param userNo    유저 번호
     * @return 해당하는 세션 요약 정보, 없으면 null
     */
    QuizSessionSummary getSummaryBySessionNoAndUserNo(@Param("sessionNo") int sessionNo,
        @Param("userNo") int userNo);

    /**
     * 같은 정답 수(correctAnswers)를 가진 세션 수를 조회한다.
     *
     * @param correctAnswers 정답 개수
     * @return 해당 정답 수를 가진 세션 수
     */
    int getCountSameSessions(int correctAnswers);

    /**
     * 같은 정답 수이면서 평균 시간이 나보다 긴 세션 수를 조회한다.
     *
     * @param correctAnswers 정답 개수
     * @param averageTime    평균 소요 시간
     * @return 나보다 느린 세션 수
     */
    int getCountSlowerSessions(@Param("correctAnswers") int correctAnswers,
        @Param("averageTime") float averageTime);
}
