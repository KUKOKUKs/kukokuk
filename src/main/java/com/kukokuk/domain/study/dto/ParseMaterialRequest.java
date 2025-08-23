package com.kukokuk.domain.study.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 학습 원본데이터 저장 요청 시 전달받는 request Dto
 */
@Getter
@Setter
public class ParseMaterialRequest {
    private List<String> urls;
}
