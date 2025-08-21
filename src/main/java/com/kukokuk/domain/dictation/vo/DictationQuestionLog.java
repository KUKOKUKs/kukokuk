package com.kukokuk.domain.dictation.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DictationQuestionLog")
public class DictationQuestionLog {

    private int dictationQuestionLogNo;  // 식별자
    private int userNo;                  // 회원번호
    private int dictationQuestionNo;     // 문제 번호
    private int dictationSessionNo;      // 문제 세트 번호
    private String userAnswer;           // 제출문장
    private int tryCount;                // 시도횟수
    private String isSuccess;            // ENUM('Y','N')
    private String usedHint;             // ENUM('Y','N'), default 'N'
    private Date createdDate;            // 생성일 (NOW())

}

