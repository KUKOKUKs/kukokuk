package com.kukokuk.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyQuest")
public class DailyQuest {

    private int dailyQuestNo;       // 일일도전과제 번호
    private String contentType;     // ENUM("QUIZ", "DICTATION", "TWENTY", "STUDY", "ESSAY")
    private String contentText;     // 컨텐츠 타이틀
    private Integer point;          // 경험치 목표 퀘스트에서만 사용
    private Integer count;          // 횟수 목표 퀘스트에서만 사용

    // 횟수를 목표로한 퀘스트인지 여부
    public boolean isCountType() {
        return count != null && count > 0 && point == null;
    }

    // 경험치량을 목표로한 퀘스트인지 여부
    public boolean isExpType() {
        return point != null && point > 0 && count == null;
    }

}