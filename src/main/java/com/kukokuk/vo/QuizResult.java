package com.kukokuk.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@Alias("QuizResult")
public class QuizResult {

    private int resultNo;
    private int sessionNo;
    private int selectedChoice;
    private String isSuccess;      // ENUM("Y","N")
    private String isBookmarked;   // ENUM("Y","N")
    private String createdDate;
    private String updatedDate;
    private int userNo;
    private int quizNo;
}
