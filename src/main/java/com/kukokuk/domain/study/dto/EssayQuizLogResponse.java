package com.kukokuk.domain.study.dto;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 논술형 퀴즈 이력 생성/수정 요청의 응답으로 전달하는 response Dto
 */
@Getter
@Setter
@NoArgsConstructor
public class EssayQuizLogResponse {
    private int dailyStudyEssayQuizLogNo;
    private String userAnswer;
    private Date createdDate;
    private Date updatedDate;
    private int dailyStudyEssayQuizNo;
}
