package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("QuizResult")
public class QuizResult {

    private int resultNo;            // 결과 고유 번호 (PK)
    private int sessionNo;           // 퀴즈 세션 번호 (FK)
    private int quizNo;              // 퀴즈 번호 (FK)
    private int userNo;              // 사용자 번호 (FK)

    private int selectedChoice;      // 사용자가 선택한 보기 번호
    private String isSuccess;        // 정답 여부 (Y / N)

    private Date createdDate;        // 생성일
    private Date updatedDate;        // 수정일
}
