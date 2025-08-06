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
     *
     * @param summary 저장할 퀴즈 세션
     * @return insert 성공 여부
     */
    int insertQuizSessionSummary(QuizSessionSummary summary);

}
