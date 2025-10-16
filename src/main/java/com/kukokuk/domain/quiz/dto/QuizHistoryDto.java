package com.kukokuk.domain.quiz.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 퀴즈 이력 DTO
 */
@Getter
@Setter
public class QuizHistoryDto {

    private int sessionNo;          // 세션 번호
    private int userNo;             // 사용자 번호
    private String gameType;        // "speed", "level", "dictation"
    private int score;              // 점수/정답 개수
    private int playTime;           // 플레이 시간(초)
    private LocalDateTime createdDate; // 생성일시

    // 단계별 퀴즈 전용
    private String difficulty;      // 난이도 (상/중/하)
    private String questionType;    // 문제유형 (뜻/단어)
}