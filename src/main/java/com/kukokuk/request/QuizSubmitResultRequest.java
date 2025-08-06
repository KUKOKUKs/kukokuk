package com.kukokuk.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmitResultRequest {
    private int quizNo;            // 퀴즈 번호
    private int selectedChoice;    // 사용자가 선택한 보기 번호
}