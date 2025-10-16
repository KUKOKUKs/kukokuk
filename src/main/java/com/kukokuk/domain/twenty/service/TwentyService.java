package com.kukokuk.domain.twenty.service;

/*import com.kukokuk.domain.twenty.util.RedisLockManager;*/
import com.kukokuk.domain.group.dto.GruopUsersDto;
import com.kukokuk.domain.group.service.GroupService;
import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.dto.SendStdMsg;
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
        twentyMapper.updateRoomStatus(map);

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
     * 게임방의 결과를 update
     * - isSuccess, tryCnt, winnerNo(게임에 승리한 경우만)를 할당한다.
     * 1. TwentyRoom 객체 결과 할당.
     *  - 객체의 status에 COMPLETED로 변경
     *  - roomNo로 log 테이블의 총 메세지 개수를 조회
     *  - roomNo로 log 테이블의 최근 메세지를 조회 : logNo, isScuccess,roomNo, userNo
     *  -> 결론: TwentyRoom 객체에 isSuccess, tryCnt, roomNo, status를할당
     * 2. 게임의 승리여부 할당
     *  - 최근 메세지의 isSuccess가 null이 아니고, Y인 경우 winnerNo를 할당. winnerNo가 null이거나,N인 경우 그냥 패스
     * 3. TwentyRoom 객체를 이제 update 쿼리문에 요청
     * 4. 사용자 상태도 모두 변경
     * roomNo를 가진 게임방의 승리여부, 시도 횟수를 업데이트, 승리여부에 따라 winnerNo가 들어가게 된다.
     */
    public void gameOverTwenty(int roomNo) {
        //최신 메세지 1개와, 총 시도 횟수를 조회.
        SendStdMsg msg = twentyMapper.getRecentMsgByRoomNo(roomNo);
        Integer tryCnt =  twentyMapper.getMsgCntByRoomNo(roomNo);

        //TwentyRoom에 값 할당하기
        TwentyRoom room = new  TwentyRoom();
        room.setTryCnt(tryCnt);
        room.setRoomNo(roomNo);
        room.setStatus("COMPLETED");
        room.setIsSuccess(msg.getIsSuccess());
        room.setWinnerNo(msg.getUserNo());

        // 게임방 변경
        twentyMapper.updateTwentyRoomResult(room);

        // 1. 사용자의 상태를 모두 '나감'으로 변경
        Map<String, Object> map = new HashMap<>();
        map.put("roomNo", roomNo);
        map.put("status", "LEFT");
        twentyMapper.updateRoomUserStatus(map);
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
    public Integer insertTwenthRoom (int groupNo, String title, String correct) {
        Map<String,Object> map = new HashMap<>();
        Integer roomNo = null;

        //게임방 생성
        TwentyRoom room = new TwentyRoom();
        room.setGroupNo(groupNo);
        room.setTitle(title);
        room.setCorrectAnswer(correct);
        room.setStatus("WAITING");
        twentyMapper.insertTwentyRoom(room);

        roomNo = room.getRoomNo();

        //groupNo에 있는 참여자들 조회 -> 참여자 테이블에 insert
        GruopUsersDto dto = groupService.getGruopUsersByGruopNo(groupNo);

        List<Integer> userNos = dto.getGroupUsers().stream()
                                                    .map(User::getUserNo)
                                                    .collect(Collectors.toList());
        map.put("roomNo", roomNo);
        map.put("status", "LEFT");
        map.put("userNos", userNos);
        twentyMapper.insertTwentyRoomUser(map);

        return roomNo;
    }


}
