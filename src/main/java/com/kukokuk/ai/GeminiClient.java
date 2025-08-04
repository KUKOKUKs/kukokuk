package com.kukokuk.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Log4j2
@Component
@RequiredArgsConstructor
public class GeminiClient {

  @Qualifier("geminiRestClient")
  private final RestClient geminiRestClient;

  /**
   * prompt를 전달하면 Gemini의 응답본문을 반환해주는 메소드
   * @param prompt Gemini에게 전달하고 싶은 프롬프트
   * @return Gemini에게 응답받은 본문 텍스트
   */
  public String getGeminiResponse(String prompt){

    // Gemini 요청 JSON 형식에 맞춘 request 객체
    GeminiRequest geminiRequest = new GeminiRequest(prompt);

    // 미리 설정해서 의존성 주입받은 geminiRestClient로 요청 전송
    GeminiResponse geminiResponse = geminiRestClient.post()
        .uri("/v1beta/models/gemini-2.0-flash:generateContent")
        .body(geminiRequest)
        .retrieve()
        // Gemini 응답 JSON 형식에 맞춘 response 객체
        .body(GeminiResponse.class);

    // 응답 객체에서 응답 본문 텍스트 부분만 추출해서 반환
    String responseText = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();

    System.out.println(geminiResponse.getUsageMetadata().getTotalTokenCount());

    return responseText;
  }
}