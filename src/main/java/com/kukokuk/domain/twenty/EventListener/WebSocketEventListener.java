package com.kukokuk.domain.twenty.EventListener;

import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.service.TwentyService;
import com.kukokuk.security.SecurityUser;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

  @Autowired
  private SimpMessagingTemplate template;

  @Autowired
  private TwentyService twentyService;

  @EventListener
  public void handleDisconnectRoom(SessionDisconnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    Principal principal = accessor.getUser();
    Integer roomNo = (Integer) accessor.getSessionAttributes().get("currentRoomNo");

    if (principal instanceof Authentication auth) {
      SecurityUser securityUser = (SecurityUser) auth.getPrincipal();
      List<String> role = securityUser.getUser().getRoleNames();
      int userNo = securityUser.getUser().getUserNo();
      if (role.contains("ROLE_TEACHER") && roomNo != null) {
        twentyService.handleTeacherDisconnect(roomNo);
        template.convertAndSend("/topic/TeacherDisconnect/" + roomNo);
      } else {
        List<RoomUser> list = twentyService.handleStudentDisconnect(roomNo, userNo);
        template.convertAndSend("/topic/participants/" + roomNo, list);
      }
    }

  }
}
