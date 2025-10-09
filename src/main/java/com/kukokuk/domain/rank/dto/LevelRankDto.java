package com.kukokuk.domain.rank.dto;

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

    private int userNo;
    private Integer userRank;
    private String nickname;
    private int level;
    private String profileFilename;
    private int experiencePoints;

}