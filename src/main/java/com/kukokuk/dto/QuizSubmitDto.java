package com.kukokuk.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmitDto {
    private Float totalTimeSec;
    private String quizMode; // 🟡 mode 추가
    private List<QuizSubmitResultDto> results;
}
