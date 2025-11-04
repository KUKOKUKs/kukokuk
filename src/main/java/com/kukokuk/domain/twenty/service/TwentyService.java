package com.kukokuk.domain.twenty.service;

/*import com.kukokuk.domain.twenty.util.RedisLockManager;*/

import com.kukokuk.domain.exp.dto.ExpProcessingDto;
import com.kukokuk.domain.exp.service.ExpProcessingService;
import com.kukokuk.domain.group.dto.GruopUsersDto;
import com.kukokuk.domain.group.service.GroupService;
import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.dto.SendStdMsg;
import com.kukokuk.domain.twenty.dto.TwentyResult;
import com.kukokuk.domain.twenty.mapper.TwentyMapper;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import com.kukokuk.domain.user.vo.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class TwentyService {

    private final TwentyMapper twentyMapper;
    private final GroupService  groupService;
    private final ExpProcessingService expProcessingService;

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
    public void updateRoomStatus(Map<String,Object> map){
        twentyMapper.updateRoomStatus(map);
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
        twentyMapper.updateRoomStatus(map);
        twentyMapper.updateRoomUserStatus(map);
    }

    /**
     * 학생이 보낸 데이터를 가지고 log 테이블에 insert 후,
     * 다시 그 객체를 반환( 식별자 포함시킨 후)
     * @param msg
     */
    public SendStdMsg insertTwentyLog(SendStdMsg msg) {
        log.info("전달 받은 msg {}",msg);
        twentyMapper.insertTwentyLog(msg);
        log.info("DB에 저장된 후 msg : {}", msg);
        return msg;
    }

    /**
     * 이 게임방의 총 메세지 개수를 가져온다.
     * @param roomNo
     * @return
     */
    public Integer getMsgCntByRoomNo(int roomNo) {
        Integer msgCnt = twentyMapper.getMsgCntByRoomNo(roomNo);
        return msgCnt;
    }

    /**
     * 이 게임방의 가장 최신 메세지를 조회
     * @param roomNo 게임방 식별자
     * @return logNo, type,userNo,content, cnt
     */
    public SendStdMsg getRecentMsgByRoomNo(int roomNo){
        SendStdMsg msg = twentyMapper.getRecentMsgByRoomNo(roomNo);
        msg.setCnt(twentyMapper.getMsgCntByRoomNo(roomNo));
        return msg;
    }

    /**
     * 교사가 응답하는 과정에서, 종료 시점이 되면 다음과 같이 스무고개의 결과를 저장한다.
     * @param msg
     */
    public void gameOver(SendStdMsg msg) {
        TwentyRoom room = new  TwentyRoom();
        room.setTryCnt(msg.getCnt());
        room.setRoomNo(msg.getRoomNo());
        room.setStatus("COMPLETED");
        room.setIsSuccess(msg.getIsSuccess());
        room.setWinnerNo(msg.getUserNo());
        twentyMapper.updateTwentyRoomResult(room);
    }

    /**
     * 메세지를 업데이트 하고, 가장 최신의 메세지 리스트를 반환
     * @param msg logNo, type,userNo,content, cnt, isSuccess, answer(질문이면 - answer, 정답이면 - isSuccess)
     * @return 메세지 리스트 : logNo, userNo, nickName, type, content
     */
    public List<SendStdMsg> updateTwentyMsgLogAndGetMsgList(SendStdMsg msg) {
        //메세지 업데이트
        twentyMapper.updateTwentyLog(msg);

        // 전체 메세지 조회
        return twentyMapper.getTwentyLogList(msg.getRoomNo());
    }

    /**
     * roomNo로 log테이블의 모든 메세지를 조회.
     * @param roomNo
     * @return
     */
    public List<SendStdMsg> getMsgListByRoomNo(int roomNo){
        return twentyMapper.getTwentyLogList(roomNo);
    }

    /**
     * 게임방과 사용자들을 DB에 insert하고, 게임방No를 반환한다.
     * @param groupNo
     * @param title
     * @param correct
     * @return
     */
    public int insertTwentyRoom (int groupNo, String title, String correct) {
        // 여기선 Integer가 반환되면 안됨 null이거나 값이 없을 경우 에러가 나는 위치가 확실해야 함
        // 여기서 에러가 발생하지 않도록 Integer로 할 경우 컨트롤러에서 404가 발생함
        // 현재위치에서 에러가 발생해야지 반환값에 대한 에러가 발생함
        // 반환타입 수정, roomNo 변수반환하지 않고 인서트되어 반환된 primary key를 반환함
        Map<String,Object> map = new HashMap<>();
//        Integer roomNo = null;

        //게임방 생성
        TwentyRoom room = new TwentyRoom();
        room.setGroupNo(groupNo);
        room.setTitle(title);
        room.setCorrectAnswer(correct);
        room.setStatus("WAITING");
        twentyMapper.insertTwentyRoom(room);

//        roomNo = room.getRoomNo();

        //groupNo에 있는 참여자들 조회 -> 참여자 테이블에 insert
        GruopUsersDto dto = groupService.getGruopUsersByGruopNo(groupNo);

        List<Integer> userNos = dto.getGroupUsers().stream()
                                                    .map(User::getUserNo)
                                                    .collect(Collectors.toList());
//        map.put("roomNo", roomNo);
        map.put("roomNo", room.getRoomNo());
        map.put("status", "LEFT");
        map.put("userNos", userNos);
        twentyMapper.insertTwentyRoomUser(map);

//        return roomNo;
        return room.getRoomNo();
    }


    /**
     * 아무 조건 없이 roomNo로 이 게임방의 모든 정보를 조회
     * @param roomNo
     * @return
     */
    public TwentyRoom getAllTwentyRoom(int roomNo){
        return twentyMapper.getAllTwentyRoom(roomNo);
    }

    /**
     * roomNo로 게임방을 (결과)조회
     * 이 게임방의 isSuccess가 Y인 경우 : 게임방의 winnerNo로 유저 정보를 조회. -> 게임방의 user 멤버에 할당
     * @param roomNo
     * @return
     */
    public TwentyResult getTwentyResultInfo(int roomNo) {

        TwentyResult result = twentyMapper.getTwentyRoomResult(roomNo);
        Integer participantCount = twentyMapper.getTwentyRoomUserTatal(roomNo);
        result.setParticipantCount(participantCount);
        log.info("getTwentyResultInfo - 게임방 결과 조회 실행완");
        return result;
    }

    /** 교사&학생 종료 버튼 클릭 시 처리
     * - roomNo로 현재 게임방을 조회해서, 게임방 상태 값을 반환
     * - COMPLETED인 경우에만 아래 경험치 부여 로직을 따른다.
     * - COMPLETED가 아니라면 그대로 return 하여 종료 한다.
     * - 로그인한 사용자 정보를 사용하여, 권한이 "ROLE_TEACHER"가 아니고,
     *   스무고개 승리 시 -> 정답자 이면 60 EXP 부여
     *                     정답자가 아니면 30EXP 부여
     *   스무고개 패배 시 -> 여지 없이 10EXP 부여
     * @param roomNo
     * @param  user : 로그인한 사용자 정보
     * @return
     */
    public TwentyRoom twentyEndProcess(int roomNo, User user) {
        TwentyRoom room = twentyMapper.getAllTwentyRoom(roomNo); // 현재 게임방 조회
        String roomStatus = room.getStatus();                    // 게임방 상태 조회
        Integer winnerNo = room.getWinnerNo();                   // 승리자 식별자
        String isSuccess = room.getIsSuccess();                  // 승리 여부
        String contentType = "TWENTY";                           // 콘텐트 타입
        ExpProcessingDto dto;                                    // 경험치 부여

        log.info("twentyEndProcess() 실행, 방 상태 : {}", roomStatus);
        log.info("twentyEndProcess() 실행, 유저 이름: {}", user.getNickname());

        // 게임이 정상 종료 상태가 아니라면, 종료.
        if(!"COMPLETED".equals(roomStatus)){
            return room;
        }
        // 교사면 EXP 처리 없이 바로 종료
        if (user.getRoleNames().contains("ROLE_TEACHER")) {
            return room;
        }

        // 학생이면 결과에 따라 EXP 지급
        if ("Y".equals(isSuccess)) {
            int exp = (user.getUserNo() == winnerNo) ? 60 : 30;
            dto = ExpProcessingDto.builder()
                .userNo(user.getUserNo())
                .contentNo(roomNo)
                .contentType(contentType)
                .expGained(exp)
                .build();
        } else {
            dto = ExpProcessingDto.builder()
                .userNo(user.getUserNo())
                .contentNo(roomNo)
                .contentType(contentType)
                .expGained(10)
                .build();
        }

        // EXP 처리
        expProcessingService.expProcessing(dto);

        return room;
    }


}
