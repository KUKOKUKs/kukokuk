package com.kukokuk.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyQuest")
public class DailyQuest {

    private int dailyQuestNo;
    private String contentType; // ENUM("QUIZ", "DICTATION", "TWENTY", "STUDY")
    private String contentText;
    private Integer point; // null 허용으로 사용될 퀘스트에서만 사용

}