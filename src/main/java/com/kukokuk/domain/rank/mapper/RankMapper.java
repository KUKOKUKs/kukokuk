package com.kukokuk.domain.rank.mapper;

import com.kukokuk.domain.rank.dto.RankRequestDto;
import com.kukokuk.domain.rank.vo.Rank;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 월별 랭킹 관련 매퍼 인터페이스
 */
@Mapper
public interface RankMapper {

    /**
     * 랭킹 등록
     * @param rank 랭킹 정보
     */
    void insertRank(Rank rank);

    /**
     * 기존 랭킹 업데이트 (점수, 플레이 횟수/조정된 값)
     * @param rank 업데이트할 랭킹 정보
     */
    void updateRank(Rank rank);

    /**
     * 특정 월, 컨텐츠타입의 사용자 랭크 조회
     * @param rankRequestDto contentType, rankMonth, userNo
     * @return 사용자의 랭크 정보
     */
    Rank getContentRankByUserNo(RankRequestDto rankRequestDto);

    /**
     * 조건에 해당하며 사용자 랭크를 포함한 랭크 목록 조회
     * <p>
     *     정확한 데이터를 가져오기 위해 RANK() 사용으로 limit 개수 보다 많을 수 있음 서비스단에서 가공 필요
     * @param rankRequestDto contentType, rankMonth, userNo, limit
     * @return 랭크 목록 정보(userRank 정렬)
     */
    List<Rank> getContentRanksIncludeUserByMonth(RankRequestDto rankRequestDto);

    /**
     * 조건에 해당하며 사용자 랭크를 포함한 그룹 랭크 목록 조회
     * <p>
     *     정확한 데이터를 가져오기 위해 RANK() 사용으로 limit 개수 보다 많을 수 있음
     *     서비스단에서 가공 필요
     * @param rankRequestDto groupNo, contentType, rankMonth, userNo, limit
     * @return 그룹 랭크 목록 정보(userRank 정렬)
     */
    List<Rank> getGroupContentRanksIncludeUserByMonth(RankRequestDto rankRequestDto);

//    /**
//     * 새로운 월별 랭킹 등록
//     * @param ranking 랭킹 정보 (rankMonth 포함)
//     */
//    void insertRanking(Ranking ranking);
//
//    /**
//     * 기존 월별 랭킹 업데이트 (점수, 플레이 횟수)
//     * @param ranking 업데이트할 랭킹 정보
//     */
//    void updateRanking(Ranking ranking);
//
//    /**
//     * 랭킹 번호로 조회
//     * @param rankNo 랭킹 번호
//     * @return 랭킹 정보
//     */
//    Ranking getRankingByRankNo(int rankNo);
//
//    /**
//     * 사용자의 특정 컨텐츠 현재 월 랭킹 조회
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @return 랭킹 정보, 없으면 null
//     */
//    Ranking getRankingByUserAndContent(@Param("userNo") int userNo, @Param("contentType") String contentType);
//
//    /**
//     * 사용자의 특정 컨텐츠 특정 월 랭킹 조회
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @param rankMonth 조회할 월 (YYYY-MM 형태)
//     * @return 랭킹 정보, 없으면 null
//     */
//    Ranking getRankingByUserContentAndMonth(@Param("userNo") int userNo,
//        @Param("contentType") String contentType,
//        @Param("rankMonth") String rankMonth);
//
//    /**
//     * 전체 월별 랭킹 목록 조회 (현재 월 기준)
//     * @param contentType 컨텐츠 타입
//     * @param limit 조회할 개수
//     * @return 랭킹 목록 (점수 높은 순)
//     */
//    List<Ranking> getGlobalRankings(@Param("contentType") String contentType, @Param("limit") int limit);
//
//    /**
//     * 전체 월별 랭킹 목록 조회 (특정 월 기준)
//     * @param contentType 컨텐츠 타입
//     * @param rankMonth 조회할 월 (YYYY-MM 형태)
//     * @param limit 조회할 개수
//     * @return 랭킹 목록 (점수 높은 순)
//     */
//    List<Ranking> getGlobalRankingsByMonth(@Param("contentType") String contentType,
//        @Param("rankMonth") String rankMonth,
//        @Param("limit") int limit);
//
//    /**
//     * 특정 그룹 내 월별 랭킹 목록 조회 (현재 월 기준)
//     * @param contentType 컨텐츠 타입
//     * @param groupNo 그룹 번호
//     * @param limit 조회할 개수
//     * @return 그룹 내 랭킹 목록 (점수 높은 순)
//     */
//    List<Ranking> getGroupRankings(@Param("contentType") String contentType,
//        @Param("groupNo") int groupNo,
//        @Param("limit") int limit);
//
//    /**
//     * 특정 그룹 내 월별 랭킹 목록 조회 (특정 월 기준)
//     * @param contentType 컨텐츠 타입
//     * @param groupNo 그룹 번호
//     * @param rankMonth 조회할 월 (YYYY-MM 형태)
//     * @param limit 조회할 개수
//     * @return 그룹 내 랭킹 목록 (점수 높은 순)
//     */
//    List<Ranking> getGroupRankingsByMonth(@Param("contentType") String contentType,
//        @Param("groupNo") int groupNo,
//        @Param("rankMonth") String rankMonth,
//        @Param("limit") int limit);
//
//    /**
//     * 전체 랭킹에서 사용자 순위 조회 (현재 월 기준)
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @return 순위 (1부터 시작)
//     */
//    int getGlobalUserRanking(@Param("userNo") int userNo, @Param("contentType") String contentType);
//
//    /**
//     * 전체 랭킹에서 사용자 순위 조회 (특정 월 기준)
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @param rankMonth 조회할 월 (YYYY-MM 형태)
//     * @return 순위 (1부터 시작)
//     */
//    int getGlobalUserRankingByMonth(@Param("userNo") int userNo,
//        @Param("contentType") String contentType,
//        @Param("rankMonth") String rankMonth);
//
//    /**
//     * 그룹 내 랭킹에서 사용자 순위 조회 (현재 월 기준)
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @param groupNo 그룹 번호
//     * @return 그룹 내 순위 (1부터 시작)
//     */
//    int getGroupUserRanking(@Param("userNo") int userNo,
//        @Param("contentType") String contentType,
//        @Param("groupNo") int groupNo);
//
//    /**
//     * 그룹 내 랭킹에서 사용자 순위 조회 (특정 월 기준)
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @param groupNo 그룹 번호
//     * @param rankMonth 조회할 월 (YYYY-MM 형태)
//     * @return 그룹 내 순위 (1부터 시작)
//     */
//    int getGroupUserRankingByMonth(@Param("userNo") int userNo,
//        @Param("contentType") String contentType,
//        @Param("groupNo") int groupNo,
//        @Param("rankMonth") String rankMonth);
//
//    /**
//     * 사용자의 모든 랭킹 삭제
//     * @param userNo 사용자 번호
//     */
//    void deleteRankingsByUser(int userNo);
//
//    /**
//     * 사용자의 월별 랭킹 히스토리 조회
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @param limit 조회할 개수 (최근 월부터)
//     * @return 월별 랭킹 히스토리 목록
//     */
//    List<Ranking> getUserMonthlyHistory(@Param("userNo") int userNo,
//        @Param("contentType") String contentType,
//        @Param("limit") int limit);

}