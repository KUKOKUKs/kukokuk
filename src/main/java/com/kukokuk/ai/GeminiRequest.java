package com.kukokuk.ai;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * { "contents": [ { "parts": [ { "text": "아래에 제시된 학습자료를 참고하여 학습 수준에 맞게 내용을 요약하고 재구성해서 JSON 배열형식으로
 * 응답해 주세요. 사용자의 학습 수준은 3단계이며, 다음 조건을 따라 주세요.\n\n1. 전체 글을 카드 단위로 요약하여 구조화해주세요.\n2. 각 카드는 'title'과
 * 'body'(여러 개의 문단, 표, 예시 포함 가능)로 구성됩니다.\n3. 카드의 개수는 3~6개 사이가 적절합니다.\n4. 각 카드의 내용은 이해하기 쉬운 문장으로
 * 요약하되, 중요한 개념과 예시는 포함시켜 주세요.\n5. 마지막에 객관식 퀴즈 4개와 서술형 퀴즈 1개를 생성해주세요.\n6. 퀴즈는 반드시 카드에서 다룬 내용 중에서
 * 출제해주세요.\n\n---\n\n[자료 내용 요약]\n\n능동적 읽기\n\n[학습목표]\n- 읽기는 글에 나타난 정보와 독자의 배경지식을 활용하여 문제를 해결하는 과정임을
 * 이해하고 글을 읽을 수 있다.\n- 자신의 읽기 과정을 점검하고 조정하며 글을 능동적으로 읽을 수 있다.\n\n[문제 해결 과정으로서의 읽기]\n- 읽기는 글을 읽으며 여러
 * 문제를 해결해나가며 의미를 구성하는 과정이다.\n- 글에 나온 정보를 단서로 자신의 배경지식을 활용해 문제를 해결해야 한다.\n- 예시: '요긴하다'의 뜻을 문맥이나 사전으로
 * 추론함\n- 예시: 문장이 모호할 경우 앞뒤 문장으로 의미 파악\n- 예시: 주제가 직접 나오지 않으면 중심 생각을 추론함\n- 예시: 글쓴이의 주장에 대해 자료를 찾아
 * 타당성 검토함\n\n[읽기 과정의 점검과 조정]\n- 읽기 전: 제목, 목차 등을 훑어보고 내용을 예측한다.\n- 읽는 중: 예측이 맞는지 확인하고 중요한 부분에 밑줄, 메모
 * 등 하며 읽는다.\n- 읽은 후: 내용을 요약하고 주제 및 글쓴이의 의도를 파악한다.\n\n[용어 정리]\n- 배경지식: 어떤 일을 하거나 연구할 때 이미 알고 있는
 * 지식\n- 문맥: 글의 앞뒤 연결\n- 모호하다: 말이나 태도가 흐리터분하여 분명하지 않다\n- 맥락: 사물이나 일이 서로 이어져 있는 관계\n- 추론하다: 판단을 바탕으로
 * 다른 판단을 이끌어내다\n" } ] } ] } 위 형태의 GeminiRequestBody를 DTO로 표현
 */
@NoArgsConstructor
@Getter
@Setter
public class GeminiRequest {

    private List<Content> contents;

    // 전달할 text 필드 값을 받는 생성자
    public GeminiRequest(String text) {
        Part part = new Part(text);
        // 요소가 하나인 경우에는 Arrays.asList() 대신 Collections.singletonList()
        Content content = new Content(Collections.singletonList(part));

        this.contents = Collections.singletonList(content);
    }

    // static 내부클래스는 외부 클래스 객체의 생성 여부와 상관없이 사용 가능
    @Getter
    @AllArgsConstructor
    private static class Content {

        private List<Part> parts;
    }

    @Getter
    @AllArgsConstructor
    private static class Part {

        public String text;
    }
}
