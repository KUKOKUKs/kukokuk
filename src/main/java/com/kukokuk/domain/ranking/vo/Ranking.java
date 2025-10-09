package com.kukokuk.domain.ranking.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 월별 랭킹 정보 VO
 * KUKOKUK_RANKS 테이블과 매핑
 */
@Getter
@Setter
@NoArgsConstructor
@Alias("Ranking")
public class Ranking {

    // 기본 필드
    private int rankNo;
    private String contentType;
    private int playCount;
    private BigDecimal totalScore;
    private String rankMonth;        // 월별 랭킹 구분 (YYYY-MM 형태)
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