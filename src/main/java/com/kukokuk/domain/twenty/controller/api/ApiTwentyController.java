package com.kukokuk.domain.twenty.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.twenty.dto.GameOverDto;
import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.dto.SendStdMsg;
import com.kukokuk.domain.twenty.service.TwentyService;
import com.kukokuk.domain.twenty.vo.TwentyLog;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<Void>> updateRoomStatus(@PathVariable int roomNo, @RequestParam String status){
        Map<String, Object> map = Map.of("roomNo", roomNo, "roomStatus", status);
        twentyService.updateRoomStaus(map);
        return ResponseEntityUtils.ok("방 상태가 성공적으로 변경.");
    }


    // 참여자 상태 변경
    @PostMapping("/room/user/status")
    public ResponseEntity<ApiResponse<Void>> updateRoomUserStatus(@RequestBody Map<String,Object> map) {
        twentyService.updateRoomUserStatus(map);
        return ResponseEntityUtils.ok("참여자 상태가 성공적으로 변경.");
    }



    // 방 참여자 목록 조회
    @GetMapping("/room/{roomNo}/players")
    public ResponseEntity<ApiResponse<List<RoomUser>>> getTwentyPlayerList(@PathVariable int roomNo) {
        List<RoomUser> list =  twentyService.getTwentyPlayerList(roomNo);
        return ResponseEntityUtils.ok(list);
    }


    // 방 정보 조회
    @GetMapping("/room/{roomNo}")
    public ResponseEntity<ApiResponse<TwentyRoom>> getTwentyRoom(@PathVariable int roomNo) {
        TwentyRoom room = twentyService.getTwentyRoomByRoomNo(roomNo);
        return ResponseEntityUtils.ok(room);
    }

    // 교사 disconnect 처리
    @PostMapping("/room/disconnect/teacher")
    public ResponseEntity<ApiResponse<Void>> handleTeacherDisconnect (@RequestBody Map<String,Object> map) {
        twentyService.handleTeacherDisconnect(map);
        return ResponseEntityUtils.ok("서버 끊긴 이벤트를 성공적으로 마쳤습니다.");
    }

    //학생이 질문 또는 정답을 제출할 때, DB에 저장-> 저장된 message 객체를 반환
    @PostMapping("/saveLog")
    public ResponseEntity<ApiResponse<SendStdMsg>> saveLog(@RequestBody SendStdMsg msg) {
        SendStdMsg log = twentyService.insertTwentyLog(msg);
        return ResponseEntityUtils.ok(log);
    }

    /**
     * 해당 게임방의 전체 메세지 개수를 조회.
     * @param roomNo
     * @return
     */
    @GetMapping("/room/{roomNo}/msgCnt")
    public ResponseEntity<ApiResponse<Integer>> getmsgCntByRoomNo(@PathVariable int roomNo) {
         Integer msgCnt = twentyService.getmsgCntByRoomNo(roomNo);
        return ResponseEntityUtils.ok(msgCnt);
    }

}
