package com.kukokuk.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("AnswerRequest")
public class AnswerRequest {
    private int quizNo;           // 퀴즈 번호
    private int userNo;           // 사용자 번호
    private int selectedOption;   // 사용자가 선택한 보기 번호
    private Integer sessionNo;    // 세션 번호 (선택사항, 스피드퀴즈 등에서 사용)
}
