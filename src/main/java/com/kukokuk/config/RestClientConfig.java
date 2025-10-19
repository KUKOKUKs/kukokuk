package com.kukokuk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Gemini 요청 시 필요한 설정 정보를 담은 RestClient 객체
     *
     * @param baseUrl Gemini 요청 베이스 경로
     * @param apiKey  Gemini API 요청 시 필요한 key
     * @return
     */
    @Bean
    public RestClient geminiRestClient(@Value("${gemini.api.baseUrl}") String baseUrl,
        @Value("${gemini.api.key}") String apiKey) {

        return RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("x-goog-api-key", apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }
}
