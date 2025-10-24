package com.kukokuk.domain.study.dto;

import com.kukokuk.domain.study.vo.DailyStudy;
import com.kukokuk.domain.study.vo.DailyStudyEssayQuiz;
import com.kukokuk.domain.study.vo.DailyStudyEssayQuizLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudyEssayViewDto {
    DailyStudy dailyStudy;
    DailyStudyEssayQuiz essay;
    DailyStudyEssayQuizLog essayLog;
}
