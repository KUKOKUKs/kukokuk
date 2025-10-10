package com.kukokuk.domain.rank.vo;

import com.kukokuk.common.util.FilePathUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * 월별 랭킹 정보 VO
 * KUKOKUK_RANKS 테이블과 매핑
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("Rank")
public class Rank {

    // 기본 필드
    private int rankNo;
    private String contentType;
    private int playCount;
    private BigDecimal totalScore;
    private String rankMonth;        // 월별 랭킹 구분 (YYYY-MM 형태)
    private int userNo;
    private Date createdDate;
    private Date updatedDate;

    private Integer userRank;

    private String nickname;
    private Integer level;
    private String profileFilename;
    private Integer experiencePoints;  // 레벨 랭킹용 경험치 필드 추가
    private Integer maxExp;  // 다음 레벨까지의 경험치

    // 프로필 이미지 경로 생성
    public String getProfileFileUrl() {
        return FilePathUtil.getProfileImagePath(userNo, profileFilename);
    }

    // BigDecimal 소수점 제거하여 정수로 표현(내림처리)
    public int getFormattedScore() {
        return totalScore == null
            ? 0
            : totalScore.setScale(0, RoundingMode.DOWN).intValue();
    }

}