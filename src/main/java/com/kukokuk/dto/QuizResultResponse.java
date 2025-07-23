package com.kukokuk.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 퀴즈 결과 상세 응답 DTO
 */
@Getter
@Setter
public class QuizResultResponse {

    private int quizNo;               // 퀴즈 고유 번호
    private String question;          // 퀴즈 질문
    private String option1;           // 보기 1
    private String option2;           // 보기 2
    private String option3;           // 보기 3
    private String option4;           // 보기 4

    private int selectedChoice;       // 사용자가 선택한 보기 번호
    private int correctAnswer;        // 실제 정답 보기 번호
    private boolean isCorrect;        // 정답 여부
    private String questionType;      // 퀴즈 유형 ("단어", "뜻")
}
