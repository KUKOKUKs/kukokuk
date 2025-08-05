package com.kukokuk.response;// 위치: com.kukokuk.response.QuizMasterResponse

import com.kukokuk.vo.QuizMaster;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuizMasterResponse {

    private int quizNo;
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String questionType;

    public static QuizMasterResponse from(QuizMaster quiz) {
        QuizMasterResponse response = new QuizMasterResponse();
        response.quizNo = quiz.getQuizNo();  // 이게 누락되면 0으로 나옴
        response.question = quiz.getQuestion();
        response.option1 = quiz.getOption1();
        response.option2 = quiz.getOption2();
        response.option3 = quiz.getOption3();
        response.option4 = quiz.getOption4();
        response.questionType = quiz.getQuestionType();
        return response;
    }
}
