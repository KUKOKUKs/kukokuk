package com.kukokuk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
    경험치 / 경험치 획득 이력 추가
    사용자 다음 레벨 조건 체크 / 사용자 레벨 증가
    일일 도전과제 관련 컨텐츠 타입 완료 체크 / 일일 도전과제 완료 내역 추가
    를 위한 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpProcessingDto {

    /*
        contentType 과 contentNo 값으로
        해당 경험치가 어떤 컨텐츠에 관련된 경험치인지 이력을 확인할 수 있도록 함
     */

    private int userNo;             // 사용자 번호
    private String contentType;     // 컨텐츠 타입(추후 다른 컨텐츠가 추가가 되더라도 사용 가능)
    private Integer contentNo;      // 컨텐츠 이력 테이블 식별자 값
    private Integer expGained;      // 획득한 경험치
    private Integer dailyQuestNo;   // 일일 도전과제 식별자 값(관련 컨텐츠일 경우 추가(아닐경우 null))

}
