package com.kukokuk.domain.ranking.mapper;

import com.kukokuk.domain.ranking.vo.Ranking;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 랭킹 관련 DB 연동 Mapper
 */
@Mapper
public interface RankingMapper {

    /**
     * 전체 랭킹 조회 (모든 사용자)
     *
     * @param contentType 컨텐츠 유형 (SPEED, LEVEL, DICTATION 등)
     * @return 랭킹 리스트 (순위 포함)
     */
    List<Ranking> getAllRankingsByContentType(@Param("contentType") String contentType);

    /**
     * 반별 랭킹 조회 (특정 그룹 내 사용자)
     *
     * @param contentType 컨텐츠 유형
     * @param groupNo 그룹 번호
     * @return 그룹 내 랭킹 리스트
     */
    List<Ranking> getGroupRankingsByContentType(@Param("contentType") String contentType,
        @Param("groupNo") int groupNo);

    /**
     * 특정 사용자의 랭킹 정보 조회
     *
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 유형
     * @return 사용자 랭킹 정보
     */
    Ranking getUserRanking(@Param("userNo") int userNo,
        @Param("contentType") String contentType);

    /**
     * 랭킹 정보 신규 등록
     *
     * @param ranking 랭킹 정보
     */
    void insertRanking(Ranking ranking);

    /**
     * 랭킹 정보 업데이트 (점수 갱신)
     *
     * @param ranking 업데이트할 랭킹 정보
     */
    void updateRanking(Ranking ranking);

    /**
     * 특정 사용자의 랭킹 존재 여부 확인
     *
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 유형
     * @return 존재하면 1, 없으면 0
     */
    int getRankingCountByUserAndContent(@Param("userNo") int userNo,
        @Param("contentType") String contentType);

    /**
     * 랭킹 삭제 (사용자 탈퇴 시 등)
     *
     * @param userNo 사용자 번호
     */
    void deleteRankingByUserNo(@Param("userNo") int userNo);
}