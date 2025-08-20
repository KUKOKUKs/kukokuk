package com.kukokuk.domain.study.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyStudyEssayQuiz")
public class DailyStudyEssayQuiz {

    private int dailyStudyEssayQuizNo;
    private String question;
    private Date createdDate;
    private Date updatedDate;
    private int dailyStudyNo;

    private DailyStudy dailyStudy;
}