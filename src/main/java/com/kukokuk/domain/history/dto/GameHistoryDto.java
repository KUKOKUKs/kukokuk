package com.kukokuk.domain.history.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class GameHistoryDto {
    private int sessionNo;
    private int userNo;
    private String gameType;      // speed, level, dictation
    private int score;
    private int playTime;
    private LocalDateTime createdDate;

    // 단계별 퀴즈용 추가 필드
    private String difficulty;     // 상, 중, 하
    private String questionType;   // 뜻, 단어
}