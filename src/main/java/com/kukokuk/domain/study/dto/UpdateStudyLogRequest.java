package com.kukokuk.domain.study.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 학습 이력 수정 시 전달받는 request Dto
 */
@Getter
@Setter
public class UpdateStudyLogRequest {
    private Integer studiedCardCount; // Null 허용
    private String status;  // "IN_PROGRESS" or "COMPLETED"
}
