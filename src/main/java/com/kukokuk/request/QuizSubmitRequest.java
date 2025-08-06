package com.kukokuk.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizSubmitRequest {
    private Float totalTimeSec;
    private List<QuizSubmitResultRequest> results;
}
