package com.kukokuk.dto;

import lombok.Getter;
import lombok.Setter;

/*
    경험치 추가를 위한 DTO
    경험치 추가 시 퀘스트 관련된 컨텐츠일 경우 DailyQuestEnum 추가
 */
@Getter
@Setter
public class ExpLogProcessingDto {

    // contentType과 contentNo 값으로
    // 해당 경험치가 어떤 컨텐츠에 관련된 경험치인지 이력을 확인할 수 있도록 함
    private int userNo;                 // 사용자 번호
    private String contentType;         // 확장성을 위한 컨텐츠 타입 입력(퀘스트가 아닌 새로운 컨텐츠 추가 시에도 활용 가능)
    private int contentNo;              // 해당 컨텐츠(각 컨텐츠별 이력 테이블 번호)
    private int expGained;              // 획득한 경험치
    private DailyQuestEnum quest;       // 퀘스트 관련된 컨텐츠일 경우 해당 enum 추가(퀘스트가 아닐 경우 null)

}
