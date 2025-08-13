package com.kukokuk.dto;

import com.kukokuk.vo.DailyStudy;
import com.kukokuk.vo.DailyStudyCard;
import com.kukokuk.vo.DailyStudyLog;
import com.kukokuk.vo.DailyStudyQuiz;
import com.kukokuk.vo.DailyStudyQuizLog;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudyCompleteViewDto {

    private DailyStudyLog log;
}
