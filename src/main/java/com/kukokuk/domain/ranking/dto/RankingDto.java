package com.kukokuk.domain.ranking.dto;

import com.kukokuk.domain.ranking.vo.Ranking;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 랭킹 데이터 전송 객체
 * 컨텐츠 타입 한글명을 포함한 확장 DTO
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class RankingDto {

    private int rankNo;
    private String contentType;
    private String contentTypeName;
    private int playCount;
    private BigDecimal totalScore;
    private String rankMonth;
    private int userNo;
    private Date createdDate;
    private Date updatedDate;

    private String nickname;
    private String profileFilename;
    private Integer groupNo;
    private String groupTitle;
    private Integer userRank;

    /**
     * Ranking VO를 RankingDto로 변환
     * @param ranking 랭킹 VO
     * @param contentTypeName 컨텐츠 타입 한글명
     */
    public RankingDto(Ranking ranking, String contentTypeName) {
        this.rankNo = ranking.getRankNo();
        this.contentType = ranking.getContentType();
        this.contentTypeName = contentTypeName;
        this.playCount = ranking.getPlayCount();
        this.totalScore = ranking.getTotalScore();
        this.rankMonth = ranking.getRankMonth();
        this.userNo = ranking.getUserNo();
        this.createdDate = ranking.getCreatedDate();
        this.updatedDate = ranking.getUpdatedDate();
        this.nickname = ranking.getNickname();
        this.profileFilename = ranking.getProfileFilename();
        this.groupNo = ranking.getGroupNo();
        this.groupTitle = ranking.getGroupTitle();
        this.userRank = ranking.getUserRank();
    }
}