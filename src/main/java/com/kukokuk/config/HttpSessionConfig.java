package com.kukokuk.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession
public class HttpSessionConfig {
    /**
     * - 원래 HttpSession을 톰캣 메모리에 저장되는 것을 Redis에 저장하게 해주는 코드임.
     * - 그래서 @AuthenticationPrincipal에서 꺼내던 것들은 그대로 사용하면됨.
     * - 웹소켓의 경우, Principal로 꺼내면 됨.
     * - 그냥 세션이 저장되는 위치만 Redis로 변경되는 것임.
     */
}

