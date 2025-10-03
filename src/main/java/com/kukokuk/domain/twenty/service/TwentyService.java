package com.kukokuk.domain.twenty.service;

/*import com.kukokuk.domain.twenty.util.RedisLockManager;*/
import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.mapper.TwentyMapper;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class TwentyService {

    private final TwentyMapper twentyMapper;

/*    private final SimpMessagingTemplate template;

    private final RedisLockManager redisLockManager;

    private final TaskScheduler taskScheduler;
    //TaskScheduler 미래에 해야되는 일을 예약해주는 객체

    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new HashMap<>();*/

    /**
     * 교사가 "게임 종료" 버튼 누를 때,
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
     *
     * @param roomNo
     * @return
     */
    public TwentyRoom getTwentyRoomByRoomNo(int roomNo) {
        return twentyMapper.getTwentyRoomByRoomNo(roomNo);
    }

    /**
     * 그룹 번호로 오늘 만들어진 스무고개방 중 최근 몇 건만 조회해오는 메소드
     *
     * @param groupNo  그룹 번호
     * @param limitCnt 제한 건 수
     * @return
     */
    public List<TwentyRoom> getRecentTodayTwentyRoomListByGroupNo(int groupNo, int limitCnt) {
        return twentyMapper.getRecentTodayTwentyRoomListByGroupNo(groupNo, limitCnt);
    }

    /**
     * 게임방의 리스트를 전달 받아 이 리스트 중 현재 열린 방을 조회
     * @param list
     * @return
     */
    public Integer getRoomNoByRoomList(List<TwentyRoom> list) {
        if(list == null || list.isEmpty()) return null;
        for(TwentyRoom room : list) {
            if(!"COMPLETED".equals(room.getStatus()) && !"STOPPED".equals(room.getStatus())) {
                return room.getRoomNo();
            }
        }
        return null;
    }

    /**
     * 방 상태 변경
     * @param map
     */
    public void updateRoomStaus(Map<String,Object> map){
        twentyMapper.updateRoomStaus(map);
    }

    /**
     * 참여자 상태 변경
     * @param map
     */
    public void updateRoomUserStatus(Map<String,Object> map){
        twentyMapper.updateRoomUserStatus(map);
    }

    /**
     * 게임방의 참여자 리스트를 조회. "참여자 명단"에 뿌릴 때 사용.
     *
     * @param roomNo
     * @return 참여자 리스트 - 이름, userNo, status
     */
    public List<RoomUser> getTwentyPlayerList(int roomNo) {
        return twentyMapper.getTwentyPlayerList(roomNo);
    }

    /**
     * 교사가 서버 끊김 or 웹 브라우저 탭을 닫을 경우
     * - 게임방 상태 변경 및 모든 유저들의 상태를 변경하는 것.
     * @param map
     */
    public void handleTeacherDisconnect(Map<String,Object> map) {
        twentyMapper.updateRoomStaus(map);
        twentyMapper.updateRoomUserStatus(map);
    }


    /* 여기 아래는 기존의 웹소켓 관련 서비스 메소드들의 주석 처리 된 것들*/

    /*    *//**
     * 1. 시스템 문장을 저장할 변수 생성 2. 게임방 상태 IN_PROGRESS로 변경 3. 현재 상태의 게임방 조회 4. 시스템 문장과 게임방의 상태값을 map에 담아
     * 브로드 캐스팅
     *
     * @param roomNo
     *//*
    public void gameStart(int roomNo) {
        String system = "스무고개를 시작합니다.";

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

    *//**
     * 게임방에 입장을 했을 경우, 1. 입장한 사용자의 상태를 JOINED로 변경 2. 이 게임방의 참여자 전체의 리스트를 조회 3. 이 게임방을 조회 4. 참여자 리스트와
     * 게임방을 map에 담아 브로드 캐스팅
     *
     * @param userNo
     * @param roomNo
     *//*
    public void joinGameRoom(int userNo, int roomNo) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomNo", roomNo);
        map.put("userNo", userNo);
        map.put("status", "JOINED");
        twentyMapper.updateRoomUserStatus(map);
        map.clear();

        List<RoomUser> list = twentyMapper.getTwentyPlayerList(roomNo);
        TwentyRoom room = twentyMapper.getTwentyRoomByRoomNo(roomNo);
        map.put("list", list);
        map.put("roomStatus", room.getStatus());
        template.convertAndSend("/topic/participants/" + roomNo, map);
    }

    *//**
     * 교사가 끊겼을 경우 1. 게임종료 누른 경우 => 그냥 종료 2. 서버가 팅기거나 웹을 닫을 경우 - 게임방 상태 STOPPED로 변경, 교사 및 모든 학생의 상태
     * LEFT로 변경 - 이 게임방의 전체 유저 조회 - 게임방을 조회 - map 객체에 담아서 브로드캐스팅(전체 유저 + 게임방 상태)
     *
     * @param roomNo
     *//*
    public void handleTeacherDisconnect(int roomNo) {
        TwentyRoom room = twentyMapper.getTwentyRoomByRoomNo(roomNo);
        Map<String, Object> map = new HashMap<>();
        if (room != null) {
            if ("COMPLETED".equals(room.getStatus())) {
                return;
            } else {
                map.put("roomNo", roomNo);
                map.put("roomStatus", "STOPPED");
                twentyMapper.updateRoomStaus(map);
                map.put("status", "LEFT");
                twentyMapper.updateRoomUserStatus(map);
                map.clear();

                List<RoomUser> list = twentyMapper.getTwentyPlayerList(roomNo);
                map.put("list", list);
                map.put("roomStatus", "STOPPED");
                template.convertAndSend("/topic/TeacherDisconnect/" + roomNo, map);
            }
        }
    }

    *//**
     * 학생이 끊겼을 경우 웹 브라우저 탭을 닫을 경우, 1. 이 학생만 상태를 LEFT로 변경 2. 최신 전체 유저 리스트를 조회 3. 이 리스트를 다시 브로드 캐스팅
     *
     * @param roomNo
     * @param userNo
     * @return
     *//*
    public void handleStudentDisconnect(int roomNo, int userNo) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomNo", roomNo);
        map.put("userNo", userNo);
        map.put("status", "LEFT");
        twentyMapper.updateRoomUserStatus(map);
        map.clear();

        List<RoomUser> list = twentyMapper.getTwentyPlayerList(roomNo);
        map.put("list", list);
        template.convertAndSend("/topic/participants/" + roomNo, map);
    }

    *//**
     * 손들기 버튼을 누르면이 메소드로 이동 1. 게임방을 AWAITING_INPUT 상태로 변경 2. 전달 받은 userNo가 빠른 처리를 한 것인지 확인. 3. true면
     * map 객체에 userNo와 변경된 게임방의 상태를 담아고 40초 제한시간 부여 4. 바로 브로드 캐스팅
     *
     * @param roomNo
     * @param userNo
     *//*
    public void raiseHand(int roomNo, int userNo, String userNicName) {
        Map<String, Object> map = new HashMap<>();
        boolean result = redisLockManager.trySetQuestioner(roomNo, userNo);     // 동시성 제어
        if (result) {
            map.put("roomNo", roomNo);
            map.put("roomStatus", "AWAITING_INPUT");
            twentyMapper.updateRoomStaus(map);
            map.clear();

            //taskScheduler.schedule(..) : 40초 뒤에 해야되는 행위를 정의해주는 메소드
            //ScheduledFuture<?> scheduledFuture : 위의 행위들을 저장해줄 수 있는 객체
            ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(() -> turnTimeout(roomNo),
                Instant.now().plusSeconds(40));
            scheduledTasks.put(roomNo, scheduledFuture);

            map.put("roomStatus", "AWAITING_INPUT");
            map.put("userNo", userNo);
            map.put("name", userNicName);
            map.put("time", 40);
            template.convertAndSend("/topic/raisehand/" + roomNo, map);
        }
    }

    *//**
     * 40초 제한 시간안에 답변을 제출하지 못한 경우 1.Redis에 저장된 1등으로 선별된 유저를 먼저 초기화 2.게임방을 조회해서, AWAITING_INPUT 인지
     * 확인. 3.IN_PROGRESS로 변경 4.system 메세지 설정, 이 방의 상태값을 map에 담아 브로드캐스팅 5.40초 제한시간 해제
     *//*
    public void turnTimeout(int roomNo) {
        Map<String, Object> map = new HashMap<>();
        redisLockManager.releaseQuestionerLock(roomNo);
        TwentyRoom room = twentyMapper.getTwentyRoomByRoomNo(roomNo);
        if ("AWAITING_INPUT".equals(room.getStatus())) {
            map.put("roomNo", roomNo);
            map.put("roomStatus", "IN_PROGRESS");
            twentyMapper.updateRoomStaus(map);
            map.clear();

            map.put("roomStatus", "IN_PROGRESS");
            template.convertAndSend("/topic/~~/" + roomNo, map);
            // 나중에 정하자..
        }
        scheduledTasks.remove(roomNo);
    }

    *//**
     * 학생이 40초 안에 질문 또는 답변을 제출했을 경우 1. 먼저 타이머를 취소 2. Redis에 저장된 1등 학생을 삭제한다. 3. 게임방의 상태를
     * "AWAITING_RESPONSE"로 변경 4. 학생이 제출한 메세지가 질문인지 답변인지 확인 -> log 테이블에 할당 5. 메세지와, 게임방의 상태 값을 map
     * 객체에 담아 브로드 캐스팅
     *//*
    public void submitAnswerOrQuestion(int roomNo, int userNo) {
        Map<String, Object> map = new HashMap<>();
        ScheduledFuture<?> scheduledTask = scheduledTasks.get(roomNo);
        if (scheduledTask != null) {
            //아까 그 40초 뒤에 일어나는 행위들을 전부 취소 시켜주는 메소드.
            scheduledTask.cancel(false);
            scheduledTasks.remove(roomNo);
        }
        redisLockManager.releaseQuestionerLock(roomNo);
        //... 기타 로직 작성
    }*/

}
