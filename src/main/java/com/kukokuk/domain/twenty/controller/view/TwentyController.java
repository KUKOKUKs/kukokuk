package com.kukokuk.domain.twenty.controller.view;

import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.dto.WsUser;
import com.kukokuk.domain.twenty.service.TwentyService;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import com.kukokuk.domain.user.vo.User;
import com.kukokuk.security.SecurityUser;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
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
@RequiredArgsConstructor
public class TwentyController {

    private final TwentyService twentyService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${ws.url}")
    private String wsUrl;

    // 학생들의 경우, 이 경로로 게임방을 이동!

    /**
     * 학생의 경우, 이 경로로 게임방을 이동!
     * 1. 먼저 분린된 웹소켓 서버에 사용자 정보를 넘기기 위해,
     *    사용자 정보 토큰화 -> Redis에 담기 -> Model에 담기
     * 2. roomNo를 사용하여 게임방 조회 -> 없다면 group 페이지로 이동
     * 3. 있다면, 게임방 페이지로 이동.(사용자 정보 및 웹소켓 주소)
     * @param roomNo
     * @param model
     * @param securityUser
     * @return
     */
    @GetMapping("/gameRoom/{roomNo}")
    public String gameRoom(@PathVariable int roomNo,
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser) {

        // 1. 사용자 정보 -> 토큰화 -> Redis에 담기

        //토큰 만들기
        String token = UUID.randomUUID().toString();
        String key = "ws:token:" +  token;

        // 필요한 정보만 빼서 DTO 객체에 담기
        User user = securityUser.getUser();
        WsUser wsUser = new WsUser(user);

        //Redis에 담기
        redisTemplate.opsForValue().set(key, wsUser,15, TimeUnit.MINUTES);
        System.out.println("Access Token 발급 완료");

        model.addAttribute("token", token);

        //2.게임방 조회 후 게임방 유무에 따라 그룹 페이지 또는 게임방 페이지로 이동
        TwentyRoom room = twentyService.getTwentyRoomByRoomNo(roomNo);
        if (room == null) {
            model.addAttribute("error", "존재하지 않는 게임방입니다.");
            return "group/main";
        }
        model.addAttribute("roomNo", 1);
        List<RoomUser> list = twentyService.getTwentyPlayerList(roomNo);
        model.addAttribute("list", list);


        model.addAttribute("userNo", user.getUserNo());
        model.addAttribute("wsUrl",wsUrl);
        return "twenty/gameRoom";

    }

    //교사의 경우, 설정을 마치고 바로 게임방으로 이동!, PostMapping을 해야된다.

//    /**
//     * - 학생이 게임방에 입장했을 대
//     *
//     * @param currentRoomNo
//     * @param principal     => 웹소켓 서버에서는 사용자 정보를 이 객체를 통해 꺼내야 한다.
//     * @parma accessor => 웹소켓 세션에 게임방 No를 담기 위해.. => 그래야 서버 끊길 때 이 게임방No를 활용할 수 있음.
//     * <p>
//     * 추가로 Http에서는 @PathVariabel을 사용하지만, 웹소켓 서버에서는 @DestinationVariable을 사용
//     */
//    @MessageMapping("/join/{currentRoomNo}")
//    public void joinGameRoom(@DestinationVariable int currentRoomNo, Principal principal,
//        StompHeaderAccessor accessor) {
//        accessor.getSessionAttributes().put("currentRoomNo", currentRoomNo);
//
//        // 웹소켓 세션으로 핸드쉐이킹한 로그인 사용자 정보를 다시 SecurityUser로 변경해서 로직 수행
//        if (principal instanceof Authentication auth) {
//            SecurityUser securityUser = (SecurityUser) auth.getPrincipal();
//            int currentUserNo = securityUser.getUser().getUserNo();
//            twentyService.joinGameRoom(currentUserNo, currentRoomNo);
//        }
//    }
//
//    /**
//     * 교사가 게임 시작 버튼을 눌렀을 때!
//     *
//     * @param currentRoomNo
//     * @param principal
//     */
//    @MessageMapping("/gameStart/{currentRoomNo}")
//    public void gameStart(@DestinationVariable int currentRoomNo, Principal principal) {
//        twentyService.gameStart(currentRoomNo);
//    }
//
//    /**
//     * 손들기 버튼을 눌렀을 때,
//     *
//     * @param currentRoomNo
//     * @param principal
//     */
//    @MessageMapping("/raisehand/{currentRoomNo}")
//    public void raiseHand(@DestinationVariable int currentRoomNo, Principal principal) {
//        if (principal instanceof Authentication auth) {
//            SecurityUser securityUser = (SecurityUser) auth.getPrincipal();
//            int userNo = securityUser.getUser().getUserNo();
//            String userNicName = securityUser.getUser().getNickname();
//            twentyService.raiseHand(currentRoomNo, userNo, userNicName);
//        }
//
//    }

}
