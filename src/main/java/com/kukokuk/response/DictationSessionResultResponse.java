package com.kukokuk.response;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DictationSessionResultResponse")
public class DictationSessionResultResponse {
    private int dictationSessionNo; // 문제 세트 번호
    private Date startDate;         // 시작 시각
    private Date endDate;           // 종료 시각
    private int correctScore;       // 정답 점수
    private int correctCount;       // 맞은 개수
    private int hintUsedCount;      // 사용한 힌트 수
    private Date createdDate;        // 생성일 (NOW())
}
