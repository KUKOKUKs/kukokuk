package com.kukokuk.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Log4j2
public class WebSocketDisconnectListener {
    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 👇 STOMP 헤더에서 token 파라미터 추출
        String token = (String) headerAccessor.getSessionAttributes().get("token");

        if (token != null) {
            String redisKey = "ws:token:" + token;
            redisTemplate.delete(redisKey);
            log.info("❌ 연결 종료 - Access Token 제거됨: {}" ,redisKey);
        } else {
            System.out.println("⚠️ 연결 종료 - token 정보를 찾을 수 없음");
            log.info("⚠️ 연결 종료 - token 정보를 찾을 수 없음");
        }
    }

}
