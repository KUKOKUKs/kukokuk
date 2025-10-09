package com.kukokuk.domain.ranking.mapper;

import com.kukokuk.domain.ranking.vo.Ranking;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 월별 랭킹 관련 매퍼 인터페이스
 */
@Mapper
public interface RankingMapper {

    /**
     * 새로운 월별 랭킹 등록
     * @param ranking 랭킹 정보 (rankMonth 포함)
     */
    void insertRanking(Ranking ranking);

    /**
     * 기존 월별 랭킹 업데이트 (점수, 플레이 횟수)
     * @param ranking 업데이트할 랭킹 정보
     */
    void updateRanking(Ranking ranking);

    /**
     * 랭킹 번호로 조회
     * @param rankNo 랭킹 번호
     * @return 랭킹 정보
     */
    Ranking getRankingByRankNo(int rankNo);

    /**
     * 사용자의 특정 컨텐츠 현재 월 랭킹 조회
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @return 랭킹 정보, 없으면 null
     */
    Ranking getRankingByUserAndContent(@Param("userNo") int userNo, @Param("contentType") String contentType);

    /**
     * 사용자의 특정 컨텐츠 특정 월 랭킹 조회
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @param rankMonth 조회할 월 (YYYY-MM 형태)
     * @return 랭킹 정보, 없으면 null
     */
    Ranking getRankingByUserContentAndMonth(@Param("userNo") int userNo,
        @Param("contentType") String contentType,
        @Param("rankMonth") String rankMonth);

    /**
     * 전체 월별 랭킹 목록 조회 (현재 월 기준)
     * @param contentType 컨텐츠 타입
     * @param limit 조회할 개수
     * @return 랭킹 목록 (점수 높은 순)
     */
    List<Ranking> getGlobalRankings(@Param("contentType") String contentType, @Param("limit") int limit);

    /**
     * 전체 월별 랭킹 목록 조회 (특정 월 기준)
     * @param contentType 컨텐츠 타입
     * @param rankMonth 조회할 월 (YYYY-MM 형태)
     * @param limit 조회할 개수
     * @return 랭킹 목록 (점수 높은 순)
     */
    List<Ranking> getGlobalRankingsByMonth(@Param("contentType") String contentType,
        @Param("rankMonth") String rankMonth,
        @Param("limit") int limit);

    /**
     * 특정 그룹 내 월별 랭킹 목록 조회 (현재 월 기준)
     * @param contentType 컨텐츠 타입
     * @param groupNo 그룹 번호
     * @param limit 조회할 개수
     * @return 그룹 내 랭킹 목록 (점수 높은 순)
     */
    List<Ranking> getGroupRankings(@Param("contentType") String contentType,
        @Param("groupNo") int groupNo,
        @Param("limit") int limit);

    /**
     * 특정 그룹 내 월별 랭킹 목록 조회 (특정 월 기준)
     * @param contentType 컨텐츠 타입
     * @param groupNo 그룹 번호
     * @param rankMonth 조회할 월 (YYYY-MM 형태)
     * @param limit 조회할 개수
     * @return 그룹 내 랭킹 목록 (점수 높은 순)
     */
    List<Ranking> getGroupRankingsByMonth(@Param("contentType") String contentType,
        @Param("groupNo") int groupNo,
        @Param("rankMonth") String rankMonth,
        @Param("limit") int limit);

    /**
     * 전체 랭킹에서 사용자 순위 조회 (현재 월 기준)
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @return 순위 (1부터 시작)
     */
    int getGlobalUserRanking(@Param("userNo") int userNo, @Param("contentType") String contentType);

    /**
     * 전체 랭킹에서 사용자 순위 조회 (특정 월 기준)
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @param rankMonth 조회할 월 (YYYY-MM 형태)
     * @return 순위 (1부터 시작)
     */
    int getGlobalUserRankingByMonth(@Param("userNo") int userNo,
        @Param("contentType") String contentType,
        @Param("rankMonth") String rankMonth);

    /**
     * 그룹 내 랭킹에서 사용자 순위 조회 (현재 월 기준)
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @param groupNo 그룹 번호
     * @return 그룹 내 순위 (1부터 시작)
     */
    int getGroupUserRanking(@Param("userNo") int userNo,
        @Param("contentType") String contentType,
        @Param("groupNo") int groupNo);

    /**
     * 그룹 내 랭킹에서 사용자 순위 조회 (특정 월 기준)
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @param groupNo 그룹 번호
     * @param rankMonth 조회할 월 (YYYY-MM 형태)
     * @return 그룹 내 순위 (1부터 시작)
     */
    int getGroupUserRankingByMonth(@Param("userNo") int userNo,
        @Param("contentType") String contentType,
        @Param("groupNo") int groupNo,
        @Param("rankMonth") String rankMonth);

    /**
     * 사용자의 모든 랭킹 삭제
     * @param userNo 사용자 번호
     */
    void deleteRankingsByUser(int userNo);

    /**
     * 사용자의 월별 랭킹 히스토리 조회
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @param limit 조회할 개수 (최근 월부터)
     * @return 월별 랭킹 히스토리 목록
     */
    List<Ranking> getUserMonthlyHistory(@Param("userNo") int userNo,
        @Param("contentType") String contentType,
        @Param("limit") int limit);

}