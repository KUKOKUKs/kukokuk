package com.kukokuk.domain.twenty.controller.view;

import com.kukokuk.domain.twenty.dto.CreateRoom;
import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.dto.TwentyResult;
import com.kukokuk.domain.twenty.dto.WsUser;
import com.kukokuk.domain.twenty.service.TwentyService;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import com.kukokuk.domain.user.vo.User;
import com.kukokuk.security.SecurityUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        model.addAttribute("roomDto", room);
        List<RoomUser> list = twentyService.getTwentyPlayerList(roomNo);
        model.addAttribute("playerList", list);

        // 타임리프에서 직접 꺼낼 수 있으므로 필요없음
//        model.addAttribute("nickName",user.getNickname());
//        model.addAttribute("userNo", user.getUserNo());
        model.addAttribute("wsUrl",wsUrl);
        return "twenty/gameRoom1";

    }

    /**
     * 교사의 경우: 게임방 페이지를 만들고, 게임방 페이지로 이동
     * 1. 폼의 값을 map에 담아 서비스 객체에 담아 -> DB에 저장 -> 그 게임방을 조회
     * 2. 그 게임방의 no를 가지고 리다이렉션으로 이동.
     * @return
     */
    @PostMapping("/gameRoom")
    public String createTwentyRoom(@Valid @ModelAttribute CreateRoom room,
                                   BindingResult bindingResult,
                                   RedirectAttributes attributes){
        //폼 제출 시 유효성 검사
        if(bindingResult.hasErrors()){
            if(bindingResult.hasFieldErrors("title")){
                attributes.addFlashAttribute("titleError",
                                                         bindingResult.getFieldError("title").getDefaultMessage());
            }

            if(bindingResult.hasFieldErrors("correct")){
                attributes.addFlashAttribute("correctError",
                                                         bindingResult.getFieldError("correct").getDefaultMessage());
            }
            return "redirect:/group/teacher";
        }

        //게임방 생성 및, 게임 참여자 DB에 할당
        int roomNo = twentyService.insertTwentyRoom(room.getGroupNo(), room.getTitle(),room.getCorrect());
        return "redirect:/twenty/gameRoom/" + roomNo;
    }

    /**
     * 결과 이력 페이지로 이동.
     * @param currentRoomNo
     * @param model
     * @return
     */
    @GetMapping("/result/{currentRoomNo}")
    public String twentyResult(@PathVariable int currentRoomNo, Model model) {
        //currentRoomNo 이 값으로 게임방 테이블의 모든 값을 조회.
        // model에 담기
        TwentyResult result = twentyService.getTwentyResultInfo(currentRoomNo);
        model.addAttribute("isSuccess",  result.getIsSuccess());
        model.addAttribute("title", result.getTitle());
        model.addAttribute("winnerName",result.getNickName());
        model.addAttribute("correctAnswer",result.getAnswers());
        model.addAttribute("tryCount",result.getTryCnt());
        model.addAttribute("participantCount",result.getParticipantCount());

        return "twenty/gameResult";
    }
}
