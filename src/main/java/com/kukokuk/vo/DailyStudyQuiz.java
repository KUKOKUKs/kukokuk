package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyStudyQuiz")
public class DailyStudyQuiz {

    private int dailyStudyQuizNo;
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private int successAnswer;
    private Date createdDate;
    private Date updatedDate;
    private int dailyStudyNo;

    private DailyStudy dailyStudy;
}