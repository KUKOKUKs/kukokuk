package com.kukokuk.domain.study.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 논술형 퀴즈 이력 생성/수정 요청 시 전달받는 request Dto
 */
@Getter
@Setter
public class EssayQuizLogRequest {
    private Integer dailyStudyEssayQuizLogNo; // 필수X AI피드백 요청에서 사용
    private Integer dailyStudyEssayQuizNo;
    private String userAnswer;
}
