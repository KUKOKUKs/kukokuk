package com.kukokuk.ai;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * {
 *     "candidates": [
 *         {
 *             "content": {
 *                 "parts": [
 *                     {
 *                        "text": "응답텍스트입니다"
 *                     }
 *                 ],
 *                 "role": "model"
 *             },
 *             "finishReason": "STOP",
 *             "avgLogprobs": -0.1634575049850292
 *         }
 *     ],
 *     "usageMetadata": {
 *         "promptTokenCount": 658,
 *         "candidatesTokenCount": 1242,
 *         "totalTokenCount": 1900,
 *         "promptTokensDetails": [
 *             {
 *                 "modality": "TEXT",
 *                 "tokenCount": 658
 *             }
 *         ],
 *         "candidatesTokensDetails": [
 *             {
 *                 "modality": "TEXT",
 *                 "tokenCount": 1242
 *             }
 *         ]
 *     },
 *     "modelVersion": "gemini-2.0-flash",
 *     "responseId": "aFyAaPOuE62E1PIPxdfYkAQ"
 * }
 * 를 표현하는 DTO 클래스
 */
@NoArgsConstructor
@Getter
public class GeminiResponse {

    private List<Candidate> candidates;
    private UsageMetadata usageMetadata;


    // static 내부클래스는 외부 클래스 객체의 생성 여부와 상관없이 사용 가능
    @Getter
    @NoArgsConstructor // Jackson은 역직렬화할 때 기본 생성자를 사용
    @AllArgsConstructor
    public static class Candidate {

        private Content content;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
    private List<Part> parts;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {

        private String text;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageMetadata {

        private int promptTokenCount;
        private int candidatesTokenCount;
        private int totalTokenCount;
    }
}
