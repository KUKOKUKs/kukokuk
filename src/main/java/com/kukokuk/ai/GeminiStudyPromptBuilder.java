package com.kukokuk.ai;


public class GeminiStudyPromptBuilder {

    /*
    수준에 맞는 학습자료를 생성하기 위한 프롬프트 전문
     */
    private static final String STUDY_PROMPT_TEMPLATE = """
          다음은 학생을 위한 학습자료입니다. 사용자의 학습 수준 설명은 다음과 같습니다:
          
          [학습 수준 설명]
          %s
          
          이 수준에 맞게 학습자료를 다음과 같은 구조로 재구성해 주세요.
          
          1. 전체 학습 자료를 이해하기 쉬운 카드(card) 형식으로 5~10개로 나눠 요약해 주세요.
          2. 각 카드는 다음과 같은 구조를 따릅니다:
             - title: 카드 제목
             - body: 다음과 같은 타입의 구성 요소 배열
               - type: "paragraph" | "table" | "example" | "definition" | "quote" | "list"
               - content:
                   - paragraph/example/definition/quote → 문자열
                   - table → 2차원 배열 [["열1", "열2"], ["내용1", "내용2"]]
                   - list → 배열 ["항목1", "항목2"]
          3. 카드 내용에서 **강조하고 싶은 내용은 마크다운처럼 `**강조 내용**`으로 감싸 주세요.**
          4. 카드 생성 후, 아래와 같은 형식의 객관식 퀴즈 4개를 생성해주세요:
             - question: 문제 내용
             - options: 보기 4개 (문자열 배열)
             - answer: 정답 보기 번호 (int)
          5. 마지막으로 서술형 퀴즈 1개를 생성해주세요:
             - question: 서술형 질문
             - evaluation_points: 채점 기준 목록
          
          **전체 응답은 반드시 아래 JSON 구조를 따라야 합니다.**
          
          {
            "mainTitle" : "학습의 메인 제목",
            "mainExplanation" : "학습의 메인 설명",
            "cards": [
              {
                "title": "카드 제목",
                "body": [
                  {
                    "type": "paragraph",
                    "content": "문단 내용입니다. **중요한 부분**은 강조해 주세요."
                  },
                  {
                    "type": "table",
                    "content": [
                      ["구분", "설명"],
                      ["A", "내용1"],
                      ["B", "내용2"]
                    ]
                  }
                ]
              }
            ],
            "quizzes": [
              {
                "question": "질문 내용",
                "options": ["보기1", "보기2", "보기3", "보기4"],
                "answer": "보기1"
              }
            ],
            "essay": {
              "question": "서술형 문제",
              "evaluationPoints": [
                "핵심 개념 설명 포함",
                "구체적인 예시 포함",
                "논리적인 전개"
              ]
            }
          }
          }
          
        이제 아래의 학습자료를 참고하여 위 구조에 맞춰 응답을 작성해 주세요.
          
        [학습자료]
        %s
        """;

    /**
     * 학습 수준 설명과 학습자료 content를 기반으로 최종 프롬프트 텍스트를 생성한다.
     *
     * @param content         학습자료 원문 JSON 배열 (문자열)
     * @param studyDifficulty 사용자 학습 수준 설명
     * @return Gemini에게 전달할 전체 프롬프트 텍스트
     */
    public static String buildDailyStudyPrompt(String content, String studyDifficulty) {
        return String.format(STUDY_PROMPT_TEMPLATE, studyDifficulty, content);
    }

    private static final String ESSAY_PROMPT_TEMPLATE = """
            당신은 국어 서술형 평가 교사입니다.
            아래 입력을 바탕으로 'sections' 배열만 포함된 JSON을 작성하세요.
            
            [평가기준]
            - 답안이 문제의 요구사항을 충족하는지 판단하세요.
            - 잘한 점을 최소 2가지 이상, 구체적 근거와 함께 제시하세요.
            - 부족한 점과 개선 방법을 구체적으로 제안하세요.
            - 학생 수준에 맞춰 문학적·비판적 사고를 자극하는 질문을 1~2개 포함하세요.
            - 정답률, 논리성, 표현력에 대해 5점 만점으로 평가하세요.
            
            [출력 요구사항]
            - JSON 최상위 키는 오직 "sections" 하나만.
            - 각 section은 {type, icon, title, items[]} 형식.
            - type은 summary | positives | improvements | questions | scores | custom 등 자유롭게 사용.
            - items는 반드시 배열이며, 각 요소는 최소 {text}를 포함.
            - 필요 시 tags/metric/value/max 등의 필드를 선택적으로 추가 가능.
            - 점수는 type="scores" 섹션의 items에 {metric, value, max}로 표현(예: {"metric":"정확성","value":4,"max":5}).
            - 마크다운/코드펜스/불필요한 텍스트 없이 **JSON만 출력**.
            - 한국어로 작성.
            
            [입력]
            - 사용자 수준: %s
            - 문제: %s
            - 학생 답안: %s
            
            [출력 예시]
            {
              "sections": [
                {
                  "type": "summary",
                  "title": "총평",
                  "items": [ { "text": "..." } ]
                }
              ]
            }
            
            [주의]
            - JSON 외 다른 텍스트 절대 출력 금지.
            - NaN/Infinity, 주석, 트레일링 콤마 금지.
        """;

    public static String buildEssayQuizPrompt(String quizContent, String userAnswer, String studyDifficulty) {
        return String.format(
            ESSAY_PROMPT_TEMPLATE,
            studyDifficulty,
            quizContent,
            userAnswer
        );
    }
}
