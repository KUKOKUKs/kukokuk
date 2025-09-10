package com.kukokuk.domain.study.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학습퀴즈 이력 생성/수정 시 전달받는 request Dto
 */
@Getter
@Setter
@NoArgsConstructor
public class StudyQuizLogRequest {

    private Integer dailyStudyQuizNo; // null 허용 (수정시 필요X)
    private int selectedChoice;
}
