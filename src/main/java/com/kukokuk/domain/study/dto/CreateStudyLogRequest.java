package com.kukokuk.domain.study.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학습 이력 생성 요청 시 전달받는 request Dto
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateStudyLogRequest {
    private int dailyStudyNo;
}
