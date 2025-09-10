package com.kukokuk.domain.ranking.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 랭킹 정보 VO
 */
@Getter
@Setter
@Alias("Ranking")
public class Ranking {

    private int rankNo;
    private String contentType;
    private int playCount;
    private BigDecimal totalScore;
    private int userNo;
    private Date createdDate;
    private Date updatedDate;

    // 조회용 추가 필드 (JOIN 결과)
    private String nickname;
    private String profileFilename;
    private Integer groupNo;
    private String groupTitle;
    private Integer userRank;

}