/*
package com.kukokuk.config;

import java.security.Principal;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  // 웹소켓 기본 설정
  @Override
  public void registerStompEndpoints (StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")                           // 웹소켓 엔드포인트 지정
        .setAllowedOrigins("http://localhost:8080")            // 이 포트로 웹소켓 사용을 허가.
        .setHandshakeHandler(new DefaultHandshakeHandler(){
          protected Principal determineCurrentPrincipal(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication != null & authentication.isAuthenticated()) {
              return authentication;
            }
            return null;
          }                                                     //Http 세션에 담긴 Principal을 WebSocket 세션에 핸드 쉐이크 하는 코드
        })
        .withSockJS();                                          //SockJS()를 사용할 수 있도록 설정
  }

  //웹소켓 실시간 송 수신 메세지 앞 주소 설정
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry.enableSimpleBroker("/topic", "/queue");
    registry.setUserDestinationPrefix("/user");
  }
}
*/
