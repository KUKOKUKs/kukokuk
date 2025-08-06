package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("QuizSessionSummary")
public class QuizSessionSummary {

    private int sessionNo;                  // 세션 고유 번호 (PK)
    private int userNo;                     // 사용자 번호 (FK)

    private float totalTimeSec;             // 총 문제 풀이 시간
    private int totalQuestion;              // 전체 문항 수
    private int correctAnswers;             // 정답 수
    private int percentile;                 // 상위 퍼센티지
    private float averageTimePerQuestion;   // 문항당 평균 소요시간

    private Date createdDate;
    private Date updatedDate;
}
