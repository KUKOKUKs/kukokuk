package com.kukokuk.domain.study.vo;

import com.kukokuk.domain.user.vo.User;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyStudyEssayQuizLog")
public class DailyStudyEssayQuizLog {

    private int dailyStudyEssayQuizLogNo;
    private String userAnswer;
    private int score;
    private String aiFeedback;
    private Date createdDate;
    private Date updatedDate;
    private int dailyStudyEssayQuizNo;
    private int userNo;

    private DailyStudyEssayQuiz dailyStudyEssayQuiz;
    private User user;
}