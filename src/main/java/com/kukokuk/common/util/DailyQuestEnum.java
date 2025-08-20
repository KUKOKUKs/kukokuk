package com.kukokuk.common.util;

import lombok.Getter;

// 어떤 클래스가 상수만으로 작성되어 있으면 반드시 class로 선언할 필요는 없다
// 객체가 상수의 집합이면 enum을 사용
@Getter
public enum DailyQuestEnum {

    COMPLETED_DAILY_STUDY(1),
    COMPLETED_DAILY_STUDY_ESSAY_QUIZ(2);

    private final int dailyQuestNo;

    // enum은 생성자를 private로 선언. 상수를 정의할 때 사용
   DailyQuestEnum(int dailyQuestNo) {
        this.dailyQuestNo = dailyQuestNo;
    }
}
