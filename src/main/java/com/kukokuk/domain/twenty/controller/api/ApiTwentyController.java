package com.kukokuk.domain.twenty.controller.api;

import com.kukokuk.domain.twenty.dto.GameOverDto;
import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.service.TwentyService;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/twenty")
@RequiredArgsConstructor
public class ApiTwentyController {

    private final TwentyService twentyService;

    /**
     * 게임 종료 버튼을 누르면, 게임방 상태 + 유저 상태 변경 게임 결과도 입력 해야됨.
     *
     * @param securityUser
     * @param gameOverDto
     */
    @PostMapping("/gameOver")
    public void gameOver(@AuthenticationPrincipal SecurityUser securityUser,
        @RequestBody GameOverDto gameOverDto) {
        twentyService.updateRoomAndUserLeft(gameOverDto.getRoomNo());

        /* - 게임 결과도 저장하는 서비스 메소드 호출
         * 1. roomNo를 가지고 스무고개 log 테이블에서 정답을 말한 유저No와 총 질문 횟수 값을 조회.
         * 2. 만일 유저No가 null이면 스무고개 테이블에 패배로 등록.
         * 3. 유저No가 null이 아니면, 스무고개 테이블에 승리로 등록 (유저no와 총 질문 횟수 값 넣기)
         */
    }

    /*
        여기 아래 부턴, kukokuk-WebSocket에서 REST API를 호출하는 문장들
     */

    // 방 상태 변경
    @PostMapping("/room/{roomNo}/status")
    public void updateRoomStatus(@PathVariable int roomNo, @RequestParam String status) {
        Map<String, Object> map = Map.of("roomNo", roomNo, "roomStatus", status);
        twentyService.updateRoomStaus(map);
    }

    // 참여자 상태 변경
    @PostMapping("/room/user/status")
    public void updateRoomUserStatus(@RequestBody Map<String, Object> req) {
        twentyService.updateRoomUserStatus(req);
    }

    // 방 참여자 목록 조회
    @GetMapping("/room/{roomNo}/players")
    public List<RoomUser> getTwentyPlayerList(@PathVariable int roomNo) {
        return twentyService.getTwentyPlayerList(roomNo);
    }

    // 방 정보 조회
    @GetMapping("/room/{roomNo}")
    public TwentyRoom getTwentyRoom(@PathVariable int roomNo) {
        return twentyService.getTwentyRoomByRoomNo(roomNo);
    }

    // 교사 disconnect 처리
    @PostMapping("/room/disconnect/teacher")
    public void handleTeacherDisconnect(@RequestBody Map<String,Object> map) {
        twentyService.handleTeacherDisconnect(map);
    }
    //-> 서비스에서(handleTeacherDisconnect)
    // twentyMapper.updateRoomStaus(map); 와
    //   twentyMapper.updateRoomUserStatus(map);를 실행하도록 한다.
}
