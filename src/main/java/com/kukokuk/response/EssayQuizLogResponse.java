package com.kukokuk.response;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EssayQuizLogResponse {
    private int dailyStudyEssayQuizLogNo;
    private String userAnswer;
    private Date createdDate;
    private Date updatedDate;
    private int dailyStudyEssayQuizNo;
}
