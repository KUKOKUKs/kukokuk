package com.kukokuk.domain.history.mapper;

import com.kukokuk.domain.history.dto.GameHistoryDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 게임 이력 관련 DB 연동 Mapper
 */
@Mapper
public interface GameHistoryMapper {

    /**
     * 스피드 퀴즈 최근 이력 조회
     * @param userNo 유저 번호
     * @param limit 조회 개수
     * @return 스피드 퀴즈 이력 리스트
     */
    List<GameHistoryDto> getRecentSpeedHistory(
        @Param("userNo") int userNo,
        @Param("limit") int limit
    );

    /**
     * 단계별 퀴즈 최근 이력 조회
     * @param userNo 유저 번호
     * @param limit 조회 개수
     * @return 단계별 퀴즈 이력 리스트
     */
    List<GameHistoryDto> getRecentLevelHistory(
        @Param("userNo") int userNo,
        @Param("limit") int limit
    );

}