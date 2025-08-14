package com.kukokuk.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/*
    오늘 획득한 경험치 이력 contentType 기준 집계
 */
@Getter
@Setter
@Alias("DailyQuestProgressAggDto")
public class DailyQuestProgressAggDto {

    private String contentType;     // ENUM("QUIZ", "DICTATION", "TWENTY", "STUDY", "ESSAY")
    private Integer expSum;         // SUM(EXP_GAINED) 경험치 합
    private Integer cnt;            // COUNT(*) 횟수

}
