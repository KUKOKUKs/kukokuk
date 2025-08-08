package com.kukokuk.dto;

import com.kukokuk.vo.QuizMaster;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuizMasterDto {
    private int quizNo;
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String questionType;

    public static QuizMasterDto from(QuizMaster quiz) {
        QuizMasterDto dto = new QuizMasterDto();
        dto.quizNo = quiz.getQuizNo();
        dto.question = quiz.getQuestion();
        dto.option1 = quiz.getOption1();
        dto.option2 = quiz.getOption2();
        dto.option3 = quiz.getOption3();
        dto.option4 = quiz.getOption4();
        dto.questionType = quiz.getQuestionType();
        return dto;
    }
}
