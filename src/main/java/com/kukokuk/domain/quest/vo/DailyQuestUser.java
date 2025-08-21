package com.kukokuk.domain.quest.vo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Alias("DailyQuestUser")
public class DailyQuestUser {

    private int dailyQuestUserNo;       // 퀘스트 완료 내역 번호
    private int dailyQuestNo;           // 퀘스트 번호
    private int userNo;                 // 사용자 번호
    private String isObtained;          // ENUM("N", "Y")
    private Date createdDate;           // 생성일자

}