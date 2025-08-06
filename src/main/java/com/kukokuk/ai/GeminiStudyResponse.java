package com.kukokuk.ai;

import com.kukokuk.ai.GeminiStudyResponse.Card.BodyElement;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GeminiStudyResponse {

    private String mainTitle;
    private String mainExplanation;
    private List<Card> cards;
    private List<Quiz> quizzes;
    private EssayQuiz essay;

    @Getter
    @Setter
    public static class Card {

        private String title;
        private List<BodyElement> body;

        @Getter
        @Setter
        public static class BodyElement {

            private String type; // "paragraph", "table", "example", "definition", "quote", "list"
            private Object content;
      /*
              content는 type에 따라 형태가 달라짐:
              - paragraph, example, definition, quote: String
              - table, list: List<List<String>> 또는 List<String>
      */
        }
    }

    @Getter
    @Setter
    public static class Quiz {

        private String question;
        private List<String> options;
        private int answer;
    }

    @Getter
    @Setter
    public static class EssayQuiz {

        private String question;
        private List<String> evaluationPoints;
    }

}
