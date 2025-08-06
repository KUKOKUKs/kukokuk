package com.kukokuk.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmitRequest {
    private Float totalTimeSec;
    private List<QuizSubmitResultRequest> results;
}
