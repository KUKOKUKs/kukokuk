package com.kukokuk.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;
// 사용자한테 겱과 전달용
@Getter
@Setter
@Alias("AnswerResponse")
public class AnswerResponse {
    private boolean isCorrect;     // 정답 여부
    private String message;        // 안내 메시지 (예: 정답입니다!)
}
