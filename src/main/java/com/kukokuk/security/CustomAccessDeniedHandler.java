package com.kukokuk.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // 요청 URI를 조회한다.
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/api/")) {	// ajax 요청
            // 응답컨텐츠 타입을 json 으로 설정한다.
            response.setContentType("application/json; charset=utf-8");
            // HTTP 응답코드를 403을 설정한다.
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            // 텍스트를 응답으로 보내기 위해 클라이언트와 연결된 스트림(파이프) 획득
            ApiResponse<Void> apiResponse
                = ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN
                , "접근 권한이 부족합니다.");

            // 자바객체 -> json 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(apiResponse);

            // json 텍스트를 응답으로 보내기
            response.getWriter().write(jsonText);

        } else {								// 일반 JSP 요청
            response.sendRedirect("/access-denied");
        }
    }
}
