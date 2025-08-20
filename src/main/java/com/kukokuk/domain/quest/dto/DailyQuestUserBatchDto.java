package com.kukokuk.domain.quest.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.type.Alias;

/*
    일일 도전과제 일괄 업데이트에 사용될 DTO
 */
@Log4j2
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Alias("DailyQuestUserBatchDto")
public class DailyQuestUserBatchDto {

    private int userNo;                         // 사용자 번호
    private List<Integer> dailyQuestUserNos;    // 업데이트할 식별자 값
    private String isObtained;                  // 업데이트할 보상 여부 값

}
