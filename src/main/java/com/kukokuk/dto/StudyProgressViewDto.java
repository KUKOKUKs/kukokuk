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
public class StudyProgressViewDto {

    private DailyStudy dailyStudy;
    private List<DailyStudyCard> cards;
    private DailyStudyLog log;
    private List<DailyStudyQuiz> quizzes; // JS용 -> 추후 리팩토링
    private List<DailyStudyQuizLog> quizLogs; // JS용 -> 추후 리팩토링
    private List<QuizWithLogDto> quizWithLogDtos; // SSR용
}
