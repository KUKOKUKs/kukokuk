package com.kukokuk.domain.rank.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 당월의 랭크 정보를 등록 또는 누적 업데이트에 필요한 정보를 담는 DTO
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankProcessingDto {

    private int userNo;                 // 사용자 번호
    private String contentType;         // 컨텐츠 타입
    private BigDecimal score;           // 절대적 점수

    // null값 방지
    public BigDecimal getScore() {
        return score == null ? BigDecimal.ZERO : score;
    }

}
