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

      //교사가 게임을 나갈 경우, 참여 중인 학생들 상태 및 게임방 상태 변경 -> 그룹 페이지로 이동
      if (role.contains("ROLE_TEACHER") && roomNo != null) {
        twentyService.handleTeacherDisconnect(roomNo);
        List<RoomUser> list = twentyService.handleStudentDisconnect(roomNo, userNo);
        template.convertAndSend("/topic/TeacherDisconnect/" + roomNo, list);
      } else {
        List<RoomUser> list = twentyService.handleStudentDisconnect(roomNo, userNo);
        template.convertAndSend("/topic/participants/" + roomNo, list);
      }
    }

  }
}
