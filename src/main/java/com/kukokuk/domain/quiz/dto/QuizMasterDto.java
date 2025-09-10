package com.kukokuk.domain.quiz.dto;

import com.kukokuk.domain.quiz.vo.QuizMaster;
import java.util.ArrayList;
import java.util.List;
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
    private List<String> options; // DTO의 구조는 단끼리 필요한 데이터만 또는 효율적으로 활용될 데이터를 가공한 객체(클라이언트에서 한번에 처리가능하도록 options 배열 처리)
    private String questionType;

    public static QuizMasterDto from(QuizMaster quiz) {
        QuizMasterDto dto = new QuizMasterDto();
        dto.quizNo = quiz.getQuizNo();
        dto.question = quiz.getQuestion();
        dto.option1 = quiz.getOption1();
        dto.option2 = quiz.getOption2();
        dto.option3 = quiz.getOption3();
        dto.option4 = quiz.getOption4();
        dto.options = new ArrayList<>(List.of(
            quiz.getOption1(),
            quiz.getOption2(),
            quiz.getOption3(),
            quiz.getOption4()
        ));
        dto.questionType = quiz.getQuestionType();
        return dto;
    }
}
