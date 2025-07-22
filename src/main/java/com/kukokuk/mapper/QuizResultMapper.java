package com.kukokuk.mapper;

import com.kukokuk.vo.QuizResult;
import org.apache.ibatis.annotations.Mapper;

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
}
