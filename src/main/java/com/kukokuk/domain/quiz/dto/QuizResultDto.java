package com.kukokuk.domain.quiz.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("QuizResultDto")
public class QuizResultDto {
    private int quizNo;
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String bookmarked; // 'Y' 또는 'N' (또는 boolean 타입도 가능)

    private boolean correct;
    private int selectedChoice;
    private int successAnswer;
    private String isSuccess;
    private String questionType;

    /**
     * optionNumber에 따라 해당 옵션을 반환하는 커스텀 getter
     */
    public String getOption(int optionNumber) {
        switch(optionNumber) {
            case 1: return option1;
            case 2: return option2;
            case 3: return option3;
            case 4: return option4;
            default: return null; // 잘못된 번호일 경우 null 반환
        }
    }

    /**
     * Y/N 문자열을 boolean으로 변환하는 커스텀 getter
     */
    public boolean isBookmarked() {
        return "Y".equals(bookmarked);
    }
}