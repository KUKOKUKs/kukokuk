package com.kukokuk.mapper;

import com.kukokuk.vo.QuizSessionSummary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 퀴즈 세션 매퍼
 */
@Mapper
public interface QuizSessionSummaryMapper {

    /**
     * 퀴즈 세션정보를 저장한다.
     * @param summary 저장할 퀴즈 세션
     * @return insert 성공 여부
     */
    int insertQuizSessionSummary(QuizSessionSummary summary);

    /**
     * 세션 요약 정보를 수정한다 (정답 수, 평균 시간 드ㅇ)
     * @param summary 수정할 세션 정보
     * @return 수정된 행 수
     */
    int updateQuizSessionSummary(QuizSessionSummary summary);

}
