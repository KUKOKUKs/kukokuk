package com.kukokuk.domain.dictation.vo;

import com.fasterxml.jackson.databind.ser.std.CalendarSerializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
    private Integer usedHintNum;       // hint1 = 1, hint2 =2, hint3= 3

    // 자바스크립트에서 번호를 받아 어떤 힌트를 사용했는지 알 수 있는 getter
    public String getHint(Integer usedHintNum) {
        return switch (usedHintNum) {
            case 1 -> hint1;
            case 2 -> hint2;
            case 3 -> hint3;
            default -> null;
        };
    }

    // getHint에서 사용한 힌트를 받아 새로고침해도 힌트를 유지할 수 있게 해줌
    public List<String> getHintChars() {
        if (usedHintNum != null) {
            return Arrays.asList(getHint(usedHintNum).split(""));
        }
        return null;
    }


}
