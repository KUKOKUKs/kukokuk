package com.kukokuk.domain.study.vo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("DailyStudyQuizLog")
public class DailyStudyQuizLog {

    private int dailyStudyQuizLogNo;
    private String isSuccess; // ENUM("Y", "N")
    private int selectedChoice;
    private Date createdDate;
    private Date updatedDate;
    private int userNo;
    private int dailyStudyQuizNo;

    private DailyStudyQuiz dailyStudyQuiz;

    public boolean isSuccess() {
        return "Y".equals(isSuccess);
    }
}