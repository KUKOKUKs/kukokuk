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

    /**
     * 레벨 기준 사용자 랭크를 포함한 상위 랭크 목록 조회
     * <p>
     *     날짜 상관없이 레벨로만 정렬하며, 로그인 사용자를 포함한 상위 랭크 조회
     *     정확한 데이터를 가져오기 위해 RANK() 사용으로 limit 개수 보다 많을 수 있음
     *     서비스단에서 가공 필요
     * @param rankRequestDto userNo, limit
     * @return 레벨 랭크 목록 정보(level 내림차순)
     */
    List<Rank> getLevelRanksIncludeUser(RankRequestDto rankRequestDto);
}