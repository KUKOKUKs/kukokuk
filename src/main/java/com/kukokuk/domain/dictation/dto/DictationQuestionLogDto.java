package com.kukokuk.domain.dictation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictationQuestionLogDto {

    private int dictationQuestionNo;     // 문제 번호
    private String userAnswer;           // 제출문장
    private int tryCount;                // 시도횟수
    private String isSuccess;            // ENUM('Y','N')
    private String usedHint;             // ENUM('Y','N')

    public boolean isUsedHint() {
        return "Y".equals(usedHint);
    }

}
