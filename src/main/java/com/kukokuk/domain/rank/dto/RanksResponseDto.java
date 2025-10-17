package com.kukokuk.domain.rank.dto;

import com.kukokuk.common.constant.ContentTypeEnum;
import com.kukokuk.domain.rank.vo.Rank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 컨텐츠별 랭킹 리스트 DTO
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RanksResponseDto {

    private int userNo;             // 요청한 사용자 번호
    private String contentType;     // 컨텐츠 타입
    private List<Rank> ranks;       // 사용자 랭크 포함 랭크 목록
    private boolean hasUserRank;    // 랭크 목록에 사용자 랭크 포함 여부

    public String getContentTypeName() {
        return ContentTypeEnum.getDescriptionByType(contentType);
    }

}