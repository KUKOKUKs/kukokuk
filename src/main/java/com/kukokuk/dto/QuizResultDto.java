package com.kukokuk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizResultDto {
    private int quizNo;
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String bookmarked; // 'Y' 또는 'N' (또는 boolean 타입도 가능)

    private boolean correct;
    private int selectedChoice;
    private int successAnswer;
    private String isSuccess;
    private String questionType;
}
