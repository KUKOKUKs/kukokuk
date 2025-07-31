package com.kukokuk.mapper;

import com.kukokuk.response.QuizResultResponse;
import com.kukokuk.vo.QuizResult;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuizResultMapper {

    /**
     * 퀴즈 결과를 저장한다.
     * @param result 퀴즈 결과 객체
     * @return insert 성공 여부
     */
    int insertQuizResult(QuizResult result);

    /**
     * 퀴즈 풀이 횟수를 +1 한다.
     * @param quizNo 대상 퀴즈 번호
     * @return update 성공 여부
     */
    int updateUsageCount(int quizNo);

    /**
     * 정답 성공 횟수를 +1 한다.
     * @param quizNo 대상 퀴즈 번호
     * @return update 성공 여부
     */
    int updateSuccessCount(int quizNo);

    /**
     * 세션 번호와 사용자 번호로 푼 퀴즈 결과를 조회한다.
     * @param sessionNo 세션 번호
     * @param userNo 사용자 번호
     * @return 퀴즈 결과 응답 리스트
     */
    List<QuizResultResponse> getQuizResultsBySession(@Param("sessionNo") int sessionNo,
        @Param("userNo") int userNo);

}