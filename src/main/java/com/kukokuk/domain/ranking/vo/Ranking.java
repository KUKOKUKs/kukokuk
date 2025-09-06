package com.kukokuk.domain.ranking.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@Alias("Ranking")
public class Ranking {

    private int rankNo;              // RANK_NO (PK)
    private String contentType;      // CONTENT_TYPE (SPEED, LEVEL, DICTATION 등)
    private int playCount;           // PLAY_COUNT (플레이 횟수)
    private double totalScore;       // TOTAL_SCORE (절대값 점수)
    private int userNo;              // USER_NO (사용자 번호)

    // 조회용 추가 필드들 (JOIN으로 가져올 데이터)
    private String nickname;         // 사용자 닉네임 (USERS 테이블에서)
    private int ranking;             // 실제 순위 (1위, 2위...)
    private String profileFilename;  // 프로필 이미지
}