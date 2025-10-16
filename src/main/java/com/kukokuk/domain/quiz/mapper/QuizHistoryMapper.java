package com.kukokuk.domain.quiz.mapper;

import com.kukokuk.domain.quiz.dto.QuizHistoryDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 퀴즈 이력 관련 DB 연동 Mapper
 */
@Mapper
public interface QuizHistoryMapper {

    /**
     * 사용자 번호로 스피드 퀴즈 최근 이력을 조회한다.
     *
     * @param userNo 사용자 번호
     * @param limit 조회할 개수
     * @return 스피드 퀴즈 이력 리스트
     */
    List<QuizHistoryDto> getSpeedHistoryByUserNoWithLimit(
        @Param("userNo") int userNo,
        @Param("limit") int limit
    );

    /**
     * 사용자 번호로 단계별 퀴즈 최근 이력을 조회한다.
     *
     * @param userNo 사용자 번호
     * @param limit 조회할 개수
     * @return 단계별 퀴즈 이력 리스트
     */
    List<QuizHistoryDto> getLevelHistoryByUserNoWithLimit(
        @Param("userNo") int userNo,
        @Param("limit") int limit
    );
}