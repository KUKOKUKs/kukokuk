package com.kukokuk.domain.study.dto;

import com.kukokuk.domain.quiz.dto.QuizWithLogDto;
import com.kukokuk.domain.study.vo.DailyStudy;
import com.kukokuk.domain.study.vo.DailyStudyCard;
import com.kukokuk.domain.study.vo.DailyStudyLog;
import com.kukokuk.domain.study.vo.DailyStudyQuiz;
import com.kukokuk.domain.study.vo.DailyStudyQuizLog;
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
