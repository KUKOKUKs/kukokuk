package com.kukokuk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictationQuestionLogDto {

    private String userAnswer;           // 제출문장
    private int tryCount;                // 시도횟수
    private String isSuccess;            // ENUM('Y','N')

}
