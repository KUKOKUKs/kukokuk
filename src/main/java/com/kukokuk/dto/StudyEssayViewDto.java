package com.kukokuk.dto;

import com.kukokuk.vo.DailyStudyEssayQuiz;
import com.kukokuk.vo.DailyStudyEssayQuizLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudyEssayViewDto {
    DailyStudyEssayQuiz essay;
    DailyStudyEssayQuizLog essayLog;
}
