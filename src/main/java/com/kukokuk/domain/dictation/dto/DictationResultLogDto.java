package com.kukokuk.domain.dictation.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DictationResultLogDto")
public class DictationResultLogDto {
    private int dictationQuestionNo;    // 문제 번호
    private String correctAnswer;       // 정답 문장 (문제 테이블에서 조인해서 가져옴)
    private String userAnswer;          // 제출 문장
    private int tryCount;               // 시도 횟수
    private String usedHint;            // 힌트 사용 여부 "Y" / "N"
    private String isSuccess;           // 정답 여부 "Y" / "N"
    private Date createdDate;            // 생성일 (NOW())
}
