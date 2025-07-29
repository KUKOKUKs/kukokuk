// 📁 com.kukokuk.request.QuizSubmitRequest.java
package com.kukokuk.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizSubmitRequest {
    private int userNo;
    private int totalTimeSec;
    private List<QuizSubmitResultRequest> results;
}
