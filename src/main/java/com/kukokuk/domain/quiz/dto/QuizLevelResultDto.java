package com.kukokuk.domain.quiz.dto;


import lombok.Getter;
import lombok.Setter;

/**
 * 세션별 난이도/문제유형 응답 DTO
 */
@Getter
@Setter
public class QuizLevelResultDto {
    private String difficulty;     // 난이도 ("쉬움", "보통", "어려움")
    private String questionType;   // 문제 유형 ("단어", "뜻")
}
