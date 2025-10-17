package com.kukokuk.domain.twenty.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.dto.SendStdMsg;
import com.kukokuk.domain.twenty.service.TwentyService;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
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
     * 게임 종료 버튼 눌렀을 때, 게임 결과 저장 및 경험치 부여
     * @param map
     * @return
     */
    @PostMapping("/gameOver")
    public ResponseEntity<ApiResponse<Void>> gameOver(@RequestBody Map<String,Object> map) {
        int roomNo = (int)map.get("roomNo");
        twentyService.gameOverTwenty(roomNo);
        //일단 보류
        twentyService.addExp(roomNo); // 스무고개방 경험치 부여 로직 expProcessing(ExpProcessingDto expProcessingDto)  사용
        return ResponseEntityUtils.ok("게임 정상 종료");
    }

    /*
        여기 아래 부턴, kukokuk-WebSocket에서 REST API를 호출하는 메소드들
     */

    /**
     * 게임방 상태 변경
     * @param roomNo 게임방 식별자
     * @param status 게임방 상태값.
     * @return 없음.
     */
    @PostMapping("/room/{roomNo}/status")
    public ResponseEntity<ApiResponse<Void>> updateRoomStatus(@PathVariable int roomNo, @RequestParam String status){
        Map<String, Object> map = Map.of("roomNo", roomNo, "roomStatus", status);
        twentyService.updateRoomStatus(map);
        return ResponseEntityUtils.ok("방 상태가 성공적으로 변경.");
    }


    /**
     * 참여자 상태 변경
     * @param map roomNo, userNo
     * @return 없음.
     */
    @PostMapping("/room/user/status")
    public ResponseEntity<ApiResponse<Void>> updateRoomUserStatus(@RequestBody Map<String,Object> map) {
        twentyService.updateRoomUserStatus(map);
        return ResponseEntityUtils.ok("참여자 상태가 성공적으로 변경.");
    }


    /**
     * 방 참여자 목록 조회
     * @param roomNo 게임방 식별자
     * @return 참여자 리스트
     */
    @GetMapping("/room/{roomNo}/players")
    public ResponseEntity<ApiResponse<List<RoomUser>>> getTwentyPlayerList(@PathVariable int roomNo) {
        List<RoomUser> list =  twentyService.getTwentyPlayerList(roomNo);
        return ResponseEntityUtils.ok(list);
    }


    /** COMPLETED, STOPPED 조건이 있는 버전
     * 게임방 1개 정보 조회
     * @param roomNo 게임방 식별자
     * @return roomNo, correctAnswer, status, winnerNo, groupNo, isSuccess, tryCnt
     */
    @GetMapping("/room/{roomNo}")
    public ResponseEntity<ApiResponse<TwentyRoom>> getTwentyRoom(@PathVariable int roomNo) {
        TwentyRoom room = twentyService.getTwentyRoomByRoomNo(roomNo);
        return ResponseEntityUtils.ok(room);
    }

    /**
     * 아무런 조건 없이, roomNo로 게임방 1개를 조회하는 쿼리.
     * @param roomNo
     * @return
     */
    @GetMapping("/room/all/{roomNo}")
    public ResponseEntity<ApiResponse<TwentyRoom>> getAllTwentyRoom(@PathVariable int roomNo) {
        TwentyRoom room = twentyService.getAllTwentyRoom(roomNo);
        return ResponseEntityUtils.ok(room);
    }

    /**
     * 교사가 서버 끊김 or 웹 브라우저 탭 닫을 때, DB 변화
     * @param map roomNo, roomStatus(방 상태), status(사용자 상태), userNos
     * @return
     */
    @PostMapping("/room/disconnect/teacher")
    public ResponseEntity<ApiResponse<Void>> handleTeacherDisconnect (@RequestBody Map<String,Object> map) {
        twentyService.handleTeacherDisconnect(map);
        return ResponseEntityUtils.ok("서버 끊긴 이벤트를 성공적으로 마쳤습니다.");
    }

    /**
     * 학생이 실시간으로 보낸 데이터 DB에 저장.
     * @param msg userNo, roomNo, type, content
     * @return SendStdMsg
     */
    @PostMapping("/saveLog")
    public ResponseEntity<ApiResponse<SendStdMsg>> saveLog(@RequestBody SendStdMsg msg) {
        SendStdMsg log = twentyService.insertTwentyLog(msg);
        return ResponseEntityUtils.ok(log);
    }

    /**
     * 해당 게임방의 전체 메세지 개수를 조회.
     * @param roomNo 게임방 식별자
     * @return 메세지 총 갯수
     */
    @GetMapping("/room/{roomNo}/msgCnt")
    public ResponseEntity<ApiResponse<Integer>> getMsgCntByRoomNo(@PathVariable int roomNo) {
         Integer msgCnt = twentyService.getMsgCntByRoomNo(roomNo);
         log.info("이 게임방의 총 전체 메세지 : {}", msgCnt );
        return ResponseEntityUtils.ok(msgCnt);
    }

    /**
     * 이 게임방의 가장 최신 메세지를 가져온다.
     * @param roomNo 게임방 식별자
     * @return logNo, type,userNo,content, cnt
     */
    @GetMapping("/room/{roomNo}/msg/recent")
    public ResponseEntity<ApiResponse<SendStdMsg>> getRecentMsgByRoomNo(@PathVariable int roomNo) {
        SendStdMsg recentMsg = twentyService.getRecentMsgByRoomNo(roomNo);
        log.info("가장 최신 메세지 발행 : {}", recentMsg);
        return ResponseEntityUtils.ok(recentMsg);
    }

    /**
     * 게임방의 결과를 update
     * roomNo를 가진 게임방의 승리여부, 시도 횟수를 업데이트, 승리여부에 따라 winnerNo가 들어가게 된다.
     * @param room : roomNo,isSuccess, tryCnt,winnerno(승리여부에 따라)
     * @return
     */
/*    @PostMapping("/room/{roomNo}/resultUpdate")
    public ResponseEntity<ApiResponse<Void>> updateTwentyRoomResult(@RequestBody TwentyRoom room) {
        twentyService.updateTwentyRoomResult(room);
        log.info("게임 종료 버튼 : 게임 결과 게임방 테이블에 정상 적으로 저장");
        return ResponseEntityUtils.ok("정상적으로 변경되었습니다.");
    }*/

    /**
     * 메세지를 업데이트 하고, 가장 최신의 메세지 리스트를 반환
     * @param msg logNo, type,userNo,content, cnt, isSuccess, answer(질문이면 - answer, 정답이면 - isSuccess)
     * @return 메세지 리스트 : logNo, userNo, nickName, type, content, cnt
     */
    @PostMapping("/updateMsgLog")
    public ResponseEntity<ApiResponse<List<SendStdMsg>>> updateTwentyMsgLogAndGetMsgList(@RequestBody SendStdMsg msg) {
        List<SendStdMsg> msgList = twentyService.updateTwentyMsgLogAndGetMsgList(msg);
        log.info("log 테이블에 메세지 업데이트 및 최신 메세지 리스트 갱신 : {}", msgList);
        return ResponseEntityUtils.ok(msgList);
    }

    /**
     * ajax로 이 게임방의 메세지 리스트를 가져온다.
     * @param roomNo
     * @return
     */
    @GetMapping("/getMsgList/{roomNo}")
    public ResponseEntity<ApiResponse<List<SendStdMsg>>> getMsgListByRoomNo(@PathVariable int roomNo) {
        List<SendStdMsg> msgList = twentyService.getMsgListByRoomNo(roomNo);
        log.info("ajax로 메세지 리스트 호출 : {}",  msgList);
        return ResponseEntityUtils.ok(msgList);
    }

}
