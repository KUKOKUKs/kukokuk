package com.kukokuk.domain.study.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackItemDto {

    private String text;                  // 본문 텍스트
    private List<String> tags;            // 태그
    private String example;               // 예시

}
