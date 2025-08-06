// 📁 com.kukokuk.request.QuizSubmitRequest.java
package com.kukokuk.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmitRequest {

    private int userNo;
    private int totalTimeSec;
    private List<QuizSubmitResult> results;
}
