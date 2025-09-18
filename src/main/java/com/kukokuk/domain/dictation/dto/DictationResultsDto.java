package com.kukokuk.domain.dictation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictationResultsDto {

    private String question;        // 정답 문장
    private boolean success;        // Y/N -> boolean
    private String userAnswer;      // 제출 문장(null 가능)
    private int dictationQuestionNo;        // 받아쓰기 문제 번호
}
