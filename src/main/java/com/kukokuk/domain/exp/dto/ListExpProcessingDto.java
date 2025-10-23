package com.kukokuk.domain.exp.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListExpProcessingDto {

    private String contentType;     // 컨텐츠 타입(추후 다른 컨텐츠가 추가가 되더라도 사용 가능)
    private Integer expGained;      // 획득한 경험치
    private Integer dailyQuestNo;   // 일일 도전과제 식별자 값(관련 컨텐츠일 경우 추가(아닐경우 null)) - 추후 확정성을 위해 추가해 둠

    List<ExpProcessingDto> expProcessingDtos;   // 컨텐츠 이력 번호와 사용자 pair를 위한 객체 리스트

}
