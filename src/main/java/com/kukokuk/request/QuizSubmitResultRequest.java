// 📁 com.kukokuk.request.QuizSubmitResult.java
package com.kukokuk.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmitResultRequest {
    private int quizNo;
    private int selectedChoice;
    private String isSuccess;     // "Y" or "N"
    private String isBookmarked;  // "Y" or "N"
}
