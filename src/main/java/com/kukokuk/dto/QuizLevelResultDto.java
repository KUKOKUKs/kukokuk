package com.kukokuk.dto;


import lombok.Getter;
import lombok.Setter;

/**
 * 세션별 난이도/문제유형 응답 DTO
 */
@Getter
@Setter
public class QuizLevelResultDto {
    private String difficulty;     // 난이도 ("상", "중", "하")
    private String questionType;   // 문제 유형 ("단어", "뜻")
}
