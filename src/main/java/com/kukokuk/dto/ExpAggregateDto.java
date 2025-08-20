package com.kukokuk.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

/*
    획득한 경험치 이력 contentType 기준 경헙치 합계, 횟수 집계
 */
@Getter
@Setter
@ToString
@Alias("ExpAggregateDto")
public class ExpAggregateDto {

    private String contentType;     // ENUM("QUIZ", "DICTATION", "TWENTY", "STUDY", "ESSAY")
    private Integer expSum;         // SUM(EXP_GAINED) 경험치 합
    private Integer cnt;            // COUNT(*) 횟수

}
