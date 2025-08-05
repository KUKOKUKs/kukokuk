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
        
        1. 전체 학습 자료를 이해하기 쉬운 카드(card) 형식으로 3~6개로 나눠 요약해 주세요.
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
           - answer: 정답 보기 번호 (1~4)
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
   * @param content 학습자료 원문 JSON 배열 (문자열)
   * @param studyDifficulty 사용자 학습 수준 설명
   * @return Gemini에게 전달할 전체 프롬프트 텍스트
   */
  public static String buildPrompt (String content, String studyDifficulty) {
    return String.format(STUDY_PROMPT_TEMPLATE, studyDifficulty, content);
  }
}
