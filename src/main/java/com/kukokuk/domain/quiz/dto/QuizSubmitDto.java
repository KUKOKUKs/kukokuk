package com.kukokuk.domain.quiz.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmitDto {

    private Float totalTimeSec;
    private String quizMode;
    private List<QuizSubmitResultDto> results;
}
