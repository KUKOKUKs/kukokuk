package com.kukokuk.domain.rank.dto;

import com.kukokuk.domain.rank.vo.Rank;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 레벨 랭킹 조회용 DTO
 * 날짜 상관없이 전체 기간 레벨 기준 랭킹
 */
@Getter
@Setter
@NoArgsConstructor
public class LevelRankDto {

    private int userNo;           // 요청한 사용자 번호
    private List<Rank> ranks;     // 레벨 랭크 목록 (maxExp 포함)



}