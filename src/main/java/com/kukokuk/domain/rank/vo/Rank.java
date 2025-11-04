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
import lombok.ToString;
import org.apache.ibatis.type.Alias;

/**
 * 월별 랭킹 정보 VO
 * KUKOKUK_RANKS 테이블과 매핑
 */
@Getter
@Setter
@Builder
@ToString
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
    private Integer minExp;                     // 레벨의 시작 경험치
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

    // null 방지
    public int getExperiencePoints() {
        if (experiencePoints == null) return 0;
        return this.experiencePoints;
    }

    // 사용자 현재 레벨의 경험치 진행률 계산
    public int getExpPercent() {
        // 매퍼에서 매핑 시 불필요한 컬럼을 제외할 경우 null이 입력되어 방지 분기처리
        if (minExp == null || maxExp == null || getExperiencePoints() == 0) return 0;
        // 분모는 (maxExp + 1) - minExp
        double percent = ((double) (getExperiencePoints() - minExp) / (maxExp - minExp)) * 100;
        // 반올림해서 정수로 변환
        return (int) Math.round(percent);
    }

    // 사용자 현재 레벨의 경험치 퍼센트 문자열
    public String getExpPercentString() {
        return getExpPercent() + "%";
    }

}