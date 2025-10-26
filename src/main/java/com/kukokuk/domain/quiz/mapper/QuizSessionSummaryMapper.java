package com.kukokuk.domain.quiz.mapper;

import com.kukokuk.domain.quiz.vo.QuizSessionSummary;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 퀴즈 세션 요약 정보를 처리하는 MyBatis 매퍼
 */
@Mapper
public interface QuizSessionSummaryMapper {

    /**
     * 사용자의 특정 컨텐츠타입의 최신 세션 목록 정보 조회
     * @param userNo 사용자 번호
     * @param quizMode 조회할 퀴즈모드
     * @param sessionCount 조회할 개수
     * @return 최근 등록 정렬된 세션 목록 정보
     */
    List<QuizSessionSummary> getQuizSessionSummaryByUserNoAndMode(
        @Param("userNo") int userNo
        , @Param("quizMode") String quizMode
        , @Param("sessionCount") int sessionCount);

    /**
     * 퀴즈 세션 요약 정보를 저장한다.
     *
     * @param summary 저장할 퀴즈 세션 요약 객체
     */
    void insertQuizSessionSummary(QuizSessionSummary summary);

    /**
     * 퀴즈 세션 요약 정보를 수정한다. 총 정답 수, 평균 시간 등을 세션 완료 후 갱신할 때 사용한다.
     *
     * @param summary 수정할 퀴즈 세션 요약 객체
     */
    void updateQuizSessionSummary(QuizSessionSummary summary);

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
     * 나보다 성적이 좋은(정답 수가 많거나, 정답 수는 같고 시간이 더 짧은) 세션 수를 조회한다.
     *
     * @param correctAnswers 내 정답 수
     * @param averageTimePerQuestion  내 평균 풀이 시간
     * @return 나보다 앞선 세션 수
     */
    int getCountBetterSessions(
        @Param("correctAnswers") int correctAnswers,
        @Param("averageTimePerQuestion") float averageTimePerQuestion
    );

    /**
     * 전체 세션 수를 조회한다.
     *
     * @return 전체 세션 수
     */
    int getTotalSessionCount();

}
