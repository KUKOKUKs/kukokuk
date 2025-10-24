package com.kukokuk.domain.quiz.dto;

import com.kukokuk.domain.study.vo.DailyStudyQuizLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    // 보기들을 한번에 꺼내 사용할 수 있도록 하는 게터
    public List<String> getOptions() {
        List<String> options = new ArrayList<>();
        if (option1 != null && !option1.isBlank()) options.add(option1);
        if (option2 != null && !option2.isBlank()) options.add(option2);
        if (option3 != null && !option3.isBlank()) options.add(option3);
        if (option4 != null && !option4.isBlank()) options.add(option4);
        return options;
    }

}
