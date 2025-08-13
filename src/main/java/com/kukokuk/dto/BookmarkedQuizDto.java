// com.kukokuk.dto.BookmarkedQuizDto.java
package com.kukokuk.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookmarkedQuizDto {
    private int bookmarkNo;
    private int quizNo;
    private LocalDateTime createdDate;

    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;

    private String questionType;   // '뜻' | '단어'
    private String difficulty;     // '상' | '중' | '하'
    private Integer usageCount;
    private Integer successCount;
    private Double accuracyRate;
}
