package com.kukokuk.domain.twenty.service;

/*import com.kukokuk.domain.twenty.util.RedisLockManager;*/
import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.dto.SendStdMsg;
import com.kukokuk.domain.twenty.mapper.TwentyMapper;
import com.kukokuk.domain.twenty.vo.TwentyLog;
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

    /**
     * 학생이 보낸 데이터를 가지고 log 테이블에 insert 후,
     * 다시 그 객체를 반환( 식별자 포함시킨 후)
     * @param msg
     */
    public SendStdMsg insertTwentyLog(SendStdMsg msg) {
        twentyMapper.insertTwentyLog(msg);
        System.out.println("msg.getLogNo(): " + msg.getLogNo());
        return msg;
    }

    /**
     * 이 게임방의 총 메세지 개수를 가져온다.
     * @param roomNo
     * @return
     */
    public Integer getmsgCntByRoomNo(int roomNo) {
        Integer msgCnt = twentyMapper.getmsgCntByRoomNo(roomNo);
        return msgCnt;
    }

    /**
     * 이 게임방의 가장 최신 메세지를 조회
     * @param roomNo 게임방 식별자
     * @return logNo, type,userNo,content, cnt
     */
    public SendStdMsg getRecentMsgByRoomNo(int roomNo){
        SendStdMsg msg = twentyMapper.getRecentMsgByRoomNo(roomNo);
        msg.setCnt(twentyMapper.getmsgCntByRoomNo(roomNo));
        return msg;
    }

    /**
     * 게임방의 결과를 update
     * roomNo를 가진 게임방의 승리여부, 시도 횟수를 업데이트, 승리여부에 따라 winnerNo가 들어가게 된다.
     * @param room :roomNo,isSuccess, tryCnt,winnerno(승리여부에 따라)
     */
    public void updateTwentyRoomResult(TwentyRoom room) {
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
     * roomNo로 사용자의 데이터를 가져온다.
     * @param roomNo
     * @return
     */
    public List<SendStdMsg> getMsgListByRoomNo(int roomNo){
        return twentyMapper.getTwentyLogList(roomNo);
    }


}
