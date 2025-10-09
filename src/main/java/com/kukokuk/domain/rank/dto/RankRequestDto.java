package com.kukokuk.domain.rank.dto;

import com.kukokuk.common.constant.PaginationEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("RankRequestDto")
public class RankRequestDto {

    @NotBlank(message = "콘텐츠 타입은 필수입니다.")
    private String contentType; // 컨텐츠 타입

    @NotBlank(message = "조회할 월은 필수입니다.(yyyy-mm)")
    @Pattern(
        regexp = "^(19|20)\\d{2}-(0[1-9]|1[0-2])$",
        message = "형식이 올바르지 않습니다. (예: 2025-10)"
    )
    private String rankMonth;   // 조회할 월("yyyy-mm")

    private Integer userNo; // 사용자 번호
    private Integer limit = PaginationEnum.COMPONENT_ROWS; // 기본값 지정 COMPONENT_ROWS = 5
    private Integer groupNo; // 그룹별 조회 시 사용

}
