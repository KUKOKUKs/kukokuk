package com.kukokuk.domain.quiz.dto;

import com.kukokuk.domain.study.vo.DailyStudyQuizLog;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizWithLogDto {
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

    private DailyStudyQuizLog dailyStudyQuizLog; // 연결된 사용자의 이력

}
