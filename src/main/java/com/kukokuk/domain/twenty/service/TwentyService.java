package com.kukokuk.domain.twenty.service;

import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.mapper.TwentyMapper;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import com.kukokuk.domain.twenty.vo.TwentyRoomUser;
import com.kukokuk.domain.user.vo.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
public class TwentyService {

  @Autowired
  private TwentyMapper twentyMapper;
  @Autowired
  private SimpMessagingTemplate template;

  private final AtomicReference<Integer> currentQuestioner = new AtomicReference<>(null);

  /**
   * 게임방의 참여자 리스트를 조회. "참여자 명단"에 뿌릴 때 사용.
   * @param roomNo
   * @return 참여자 리스트 - 이름, userNo, status
   */
  public List<RoomUser> getTwentyPlayerList(int roomNo) {
    return twentyMapper.getTwentyPlayerList(roomNo);
  }

  /**
   * 교사가 게임 종료 버튼 누를 때,
   * 1.roomNo로 게임방 상태를 COMPLETED로 변경
   * 2.모든 유저의 상태 => LEFT로 변경
   * @param roomNo
   */
  public void updateRoomAndUserLeft(int roomNo) {
    Map<String, Object> map = new HashMap<>();
    map.put("roomNo", roomNo);
    map.put("roomStatus", "COMPLETED");
    twentyMapper.updateRoomStaus(map);

    map.put("status", "LEFT");
    twentyMapper.updateRoomUserStatus(map);
  }

  /**
   * roomNo를 가진 게임방 자체를 조회해온다.
   * @param roomNo
   * @return
   */
  public TwentyRoom getTwentyRoomByRoomNo(int roomNo) {
    return twentyMapper.getTwentyRoomByRoomNo(roomNo);
  }

  /**
   * 1. 시스템 문장을 저장할 변수 생성
   * 2. 게임방 상태 IN_PROGRESS로 변경
   * 3. 현재 상태의 게임방 조회
   * 4. 시스템 문장과 게임방의 상태값을 map에 담아 브로드 캐스팅
   * @param roomNo
   */
  public void gameStart(int roomNo){
    String system = "게임을 시작하겠습니다.";

    Map<String, Object> map = new HashMap<>();
    map.put("roomNo", roomNo);
    map.put("roomStatus", "IN_PROGRESS");
    twentyMapper.updateRoomStaus(map);
    map.clear();

    TwentyRoom room = twentyMapper.getTwentyRoomByRoomNo(roomNo);
    map.put("roomStatus", room.getStatus());
    map.put("system", system);
    template.convertAndSend("/topic/gameStart/" + roomNo, map);
  }

  /**
   * 게임방에 입장을 했을 경우,
   * 1. 입장한 사용자의 상태를 JOINED로 변경
   * 2. 이 게임방의 참여자 전체의 리스트를 조회
   * 3. 이 게임방을 조회
   * 4. 참여자 리스트와 게임방을 map에 담아 브로드 캐스팅
   * @param userNo
   * @param roomNo
   */
  public void joinGameRoom(int userNo, int roomNo) {
    Map<String, Object> map = new HashMap<>();
    map.put("roomNo", roomNo);
    map.put("userNo", userNo);
    map.put("status", "JOINED");
    twentyMapper.updateRoomUserStatus(map);
    map.clear();

    List <RoomUser> list = twentyMapper.getTwentyPlayerList(roomNo);
    TwentyRoom room = twentyMapper.getTwentyRoomByRoomNo(roomNo);
    map.put("list",list);
    map.put("roomStatus", room.getStatus());
    template.convertAndSend("/topic/participants/" + roomNo, map);
  }
  /**
   * 교사가 끊겼을 경우
   * 1. 게임종료 누른 경우 => 그냥 종료
   * 2. 서버가 팅기거나 웹을 닫을 경우
   *  - 게임방 상태 STOPPED로 변경, 교사 및 모든 학생의 상태 LEFT로 변경
   *  - 이 게임방의 전체 유저 조회
   *  - 게임방을 조회
   *  - map 객체에 담아서 브로드캐스팅(전체 유저 + 게임방 상태)
   * @param roomNo
   */
  public void handleTeacherDisconnect(int roomNo) {
    TwentyRoom room = twentyMapper.getTwentyRoomByRoomNo(roomNo);
    Map<String, Object> map = new HashMap<>();
    if (room != null) {
      if (room.getStatus() == "COMPLETED") {
        return;
      } else {
        map.put("roomNo", roomNo);
        map.put("roomStatus", "STOPPED");
        twentyMapper.updateRoomStaus(map);
        map.put("status", "LEFT");
        twentyMapper.updateRoomUserStatus(map);
        map.clear();

        List<RoomUser> list = twentyMapper.getTwentyPlayerList(roomNo);
        TwentyRoom updateRoom = twentyMapper.getTwentyRoomByRoomNo(roomNo);
        map.put("list",list);
        map.put("roomStatus", updateRoom.getStatus());
        template.convertAndSend("/topic/TeacherDisconnect/" + roomNo, map);
      }
    }
  }

  /**
   * 학생이 끊겼을 경우
   * 웹 브라우저 탭을 닫을 경우,
   * 1. 이 학생만 상태를 LEFT로 변경
   * 2. 최신 전체 유저 리스트를 조회
   * 3. 이 리스트를 다시 브로드 캐스팅
   * @param roomNo
   * @param userNo
   * @return
   */
  public void handleStudentDisconnect(int roomNo, int userNo) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("roomNo", roomNo);
    map.put("userNo", userNo);
    map.put("status", "LEFT");
    twentyMapper.updateRoomUserStatus(map);

    List<RoomUser> list = twentyMapper.getTwentyPlayerList(roomNo);
    template.convertAndSend("/topic/participants/" + roomNo, list);
  }

  /**
   * 손들기 버튼을 누르면이 메소드로 이동
   * 1. 게임방을 AWAITING_INPUT 상태로 변경
   * 2. 전달 받은 userNo가 빠른 처리를 한 것인지 확인.
   * 3. true면 map 객체에 userNo와 변경된 게임방의 상태를 담아고 40초 제한시간 부여
   * 4. 바로 브로드 캐스팅
   * @param roomNo
   * @param userNo
   */
  public void raiseHand(int roomNo, int userNo) {
    Map<String, Object> map = new HashMap<>();
    map.put("roomNo", roomNo);
    map.put("roomStatus", "AWAITING_INPUT");
    twentyMapper.updateRoomStaus(map);
    map.clear();

    boolean result = currentQuestioner.compareAndSet(null, userNo);         // 가장 빨리 처리가 유저인지 확인하는 코드임.
    map.put("roomStatus", "AWAITING_INPUT");
    if(result) {
      map.put("userNo",userNo);
      template.convertAndSend("/topic/raisehand/" + roomNo, map);
    }
  }
}
