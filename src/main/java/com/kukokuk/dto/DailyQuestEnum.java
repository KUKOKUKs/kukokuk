package com.kukokuk.dto;

import lombok.Getter;

@Getter
public enum DailyQuestEnum {

    // 일일 도전 과제 테이블 컨텐츠별 식별자 값git
    DAILY_STUDY_CHAPTER(1),     // 일일학습 한챕터 완료
    DAILY_STUDY_WRITING(2),     // 일일학습 서술형 퀴즈 1회 완료
    QUIZ_SPEED_3_TIMES(3),      // 스피드 퀴즈 3회 플레이
    QUIZ_STAGE_ONCE(4),         // 단계별 퀴즈 1회 플레이
    DICTATION_ONCE(5),          // 단계별 퀴즈 1회 플레이
    DICTATION_200_EXP(6);       // 받아쓰기로 경험치 200EXP 획득
    
    private final int quest;
    
    DailyQuestEnum(int quest) {
        this.quest = quest;
    }

}
