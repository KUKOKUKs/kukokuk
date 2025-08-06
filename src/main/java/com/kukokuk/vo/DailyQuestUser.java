package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyQuestUser")
@NoArgsConstructor
public class DailyQuestUser {

    private int dailyQuestUserNo;
    private int dailyQuestNo;
    private int userNo;
    private String isObtained; // ENUM("N", "Y")
    private Date createdDate;

    private User user;
    private DailyQuest dailyQuest;
}