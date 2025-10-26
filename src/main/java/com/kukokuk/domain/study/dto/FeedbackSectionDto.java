package com.kukokuk.domain.study.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackSectionDto {

    private String icon;              // 섹션 아이콘 (이모지)
    private String type;              // 섹션 타입 (summary, positives, improvements, questions 등)
    private String title;             // 섹션 제목
    private List<FeedbackItemDto> items;  // 섹션 아이템 목록

}
