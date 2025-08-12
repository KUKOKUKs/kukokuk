package com.kukokuk.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EssayQuizLogRequest {
    private Integer dailyStudyEssayQuizLogNo; // 필수X AI피드백 요청에서 사용
    private Integer dailyStudyEssayQuizNo;
    private String userAnswer;
}
