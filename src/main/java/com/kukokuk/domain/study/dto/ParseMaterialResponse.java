package com.kukokuk.domain.study.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학습 이력 수정 요청의 응답으로 전달하는 response Dto
 */
@Getter
@Setter
@NoArgsConstructor
public class ParseMaterialResponse {
    private List<String> skippedUrls;
    private List<String> enqueuedUrls;
}
