package com.kukokuk.domain.dictation.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DictationQuestion")
public class DictationQuestion {

    private int dictationQuestionNo;   // 문제 번호
    private String correctAnswer;      // 정답 문장
    private String hint1;              // 띄어쓰기 힌트
    private String hint2;              // 초성 힌트
    private String hint3;              // 한글자 힌트
    private Date createdDate;          // 생성일 (NOW())
    private Date updatedDate;          // 수정일 (ON UPDATE NOW())

}
