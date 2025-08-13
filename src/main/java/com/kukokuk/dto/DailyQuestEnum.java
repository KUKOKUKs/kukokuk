package com.kukokuk.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
    퀘스트 종류별 기본 룰과 메타데이터 관리 및 이력 등록 정보로 활용
 */
@Getter
@AllArgsConstructor
public enum DailyQuestEnum {

    STUDY_LEARNING(1, "STUDY", "일일학습 한챕터 완료", ProgressType.COUNT, 1),
    STUDY_ESSAY(2, "ESSAY", "일일학습 서술형 퀴즈 1회 완료", ProgressType.COUNT, 1),
    QUIZ_SPEED(3, "QUIZ", "스피드 퀴즈 3회 플레이", ProgressType.COUNT, 3),
    QUIZ_STEP(4, "QUIZ", "단계별 퀴즈 1회 플레이", ProgressType.COUNT, 1),      // 여기 추가
    DICTATION_PLAY(5, "DICTATION", "받아쓰기 1회 플레이", ProgressType.COUNT, 1),
    DICTATION_EXP(6, "DICTATION", "받아쓰기로 경험치 200EXP 획득", ProgressType.EXP, 200);

    private final int dailyQuestNo;             // 일일도전과제 식별값
    private final String type;                  // 일일도전과제 타이틀
    private final String text;                  // ENUM("QUIZ", "DICTATION", "TWENTY", "STUDY", "ESSAY")
    private final ProgressType progressType;    // COUNT, EXP
    private final int targetValue;              // 목표 횟수 또는 목표 경험치

    // 확장성을 고려하여 내부에 중첩 enum(nested enum)로 정의하여 관리
    public enum ProgressType {
        COUNT,      // 횟수 기준
        EXP         // 경험치량 기준
    }

    // 불변 Map 초기화
    private static final Map<Integer, DailyQuestEnum> BY_NO =
        Collections.unmodifiableMap(
            Arrays.stream(values())
                .collect(Collectors.toMap(DailyQuestEnum::getDailyQuestNo, e -> e))
        );

    // 식별값으로 Enum 조회
    public static DailyQuestEnum getByNo(int no) {
        return BY_NO.get(no); // 존재하지 않으면 null 반환
    }

}
