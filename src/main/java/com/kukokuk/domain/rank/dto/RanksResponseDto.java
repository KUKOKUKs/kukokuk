package com.kukokuk.domain.rank.dto;

import com.kukokuk.common.util.ContentTypeUtil;
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

    private String contentType;
    private List<Rank> ranks;

    public String getContentTypeName() {
        return ContentTypeUtil.getContentTypeName(contentType);
    }

}