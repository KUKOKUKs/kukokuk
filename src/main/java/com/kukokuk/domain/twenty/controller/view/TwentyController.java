package com.kukokuk.domain.twenty.controller.view;

import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.service.TwentyService;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import com.kukokuk.domain.twenty.vo.TwentyRoomUser;
import com.kukokuk.security.SecurityUser;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@RequestMapping("/twenty")
public class TwentyController {

  @Autowired
  private TwentyService twentyService;

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  // 학생들의 경우, 이 경로로 게임방을 이동!
  @GetMapping("/gameRoom/{roomNo}")
  public String gameRoom(@PathVariable int roomNo,
                          Model model,
                          @AuthenticationPrincipal SecurityUser securityUser) {

    TwentyRoom room = twentyService.getTwentyRoomByRoomNo(roomNo);
    if(room == null) {
      model.addAttribute("error","존재하지 않는 게임방입니다.");
      return "group/main";
    }
      model.addAttribute("roomNo", 1);
      List<RoomUser> list = twentyService.getTwentyPlayerList(roomNo);
      model.addAttribute("list", list);

      int userNo = securityUser.getUser().getUserNo();
      model.addAttribute("userNo", userNo);
      return "twenty/gameRoom";

  }

  //교사의 경우, 설정을 마치고 바로 게임방으로 이동!, PostMapping을 해야된다.

  /**
   * 1. 로그인한 사용자가 해당 게임방을 입장하면, 사용자의 userNo를 가져온 다음 그 사용자의 상태를 변경 2. roomNo로 최신의 게임방 참여자를 조회하여 브로드
   * 캐스팅
   *
   * @param currentRoomNo
   * @param principal     => 웹소켓 서버에서는 사용자 정보를 이 객체를 통해 꺼내야 한다.
   * @parma accessor => 웹소켓 세션에 부가적인 정보를 저장하기 위한 객체
   * <p>
   * 추가로 Http에서는 @PathVariabel을 사용하지만, 웹소켓 서버에서는 @DestinationVariable을 사용
   */
  @MessageMapping("/join/{currentRoomNo}")
  public void joinGameroom(@DestinationVariable int currentRoomNo, Principal principal,
      StompHeaderAccessor accessor) {
    accessor.getSessionAttributes().put("currentRoomNo", currentRoomNo);

    // 웹소켓 세션으로 핸드쉐이킹한 로그인 사용자 정보를 다시 SecurityUser로 변경해서 로직 수행
    if (principal instanceof Authentication auth) {
      SecurityUser securityUser = (SecurityUser) auth.getPrincipal();
      int currentUserNo = securityUser.getUser().getUserNo();
      twentyService.updateRoomUserJoin(currentUserNo, currentRoomNo);

      List<RoomUser> list = twentyService.getTwentyPlayerList(currentRoomNo);
      messagingTemplate.convertAndSend("/topic/participants/" + currentRoomNo, list);
    }
  }

  /**
   * 교사가 게임 시작 버튼을 눌렀을 때, 게임방을 업데이트, "게임이 시작됩니다." 메세지 브로드 캐스팅
   *
   * @param currentRoomNo
   * @param principal
   */
  @MessageMapping("/gameStart/{currentRoomNo}")
  public void gameStart(@DestinationVariable int currentRoomNo, Principal principal) {
    Map<String, Object> map = new HashMap<>();
    String systemMessage = "게임을 시작하겠습니다.";
    map.put("system", systemMessage);
    if (principal instanceof Authentication auth) {
      SecurityUser securityUser = (SecurityUser) auth.getPrincipal();
      List<String> roles = securityUser.getUser().getRoleNames();

      if (roles.contains("ROLE_TEACHER")) {
        twentyService.updateRoomStausInProgess(currentRoomNo);
        messagingTemplate.convertAndSend("/topic/gameStart/" + currentRoomNo, map);
      }

    }
  }
}
