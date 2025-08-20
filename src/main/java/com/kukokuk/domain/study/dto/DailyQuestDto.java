package com.kukokuk.domain.study.dto;

import com.kukokuk.domain.quest.vo.DailyQuestUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// 삭제 예정
public class DailyQuestDto {

    private int dailyQuestNo;
    private String contentType; // ENUM("QUIZ", "DICTATION", "TWENTY", "STUDY")
    private String contentText;

    private DailyQuestUser dailyQuestUser; // 선택적: 상세 정보 필요 시
    private boolean isSuccessed; // 버튼 상태용
    
}

