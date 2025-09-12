package com.kukokuk.domain.twenty.service;

import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.mapper.TwentyMapper;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import com.kukokuk.domain.twenty.vo.TwentyRoomUser;
import com.kukokuk.domain.user.vo.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
public class TwentyService {
  @Autowired
  private TwentyMapper twentyMapper;
  /**
   * 게임방의 참여자 리스트를 조회.
   * "참여자 명단"에 뿌릴 때 사용.
   * @param roomNo
   * @return 참여자 리스트 - 이름, userNo, status
   */
  public List<RoomUser> getTwentyPlayerList(int roomNo) {
    return twentyMapper.getTwentyPlayerList(roomNo);
  }

  /**
   * 이 게임방의 사용자의 상태를 "JOIN"으로 변경
   * @param userNo
   * @param roomNo
   */
  public void updateTwentyRoomUser(int userNo, int roomNo) {
    Map<String,Object> map = new HashMap<>();
    map.put("roomNo", roomNo);
    map.put("userNo", userNo);
    map.put("status", "JOINED");
    twentyMapper.updateRoomUserStatus(map);
  }

  /**
   * 교사가 게임 종료 버튼 누를 때, roomNo로 게임방 상태를 변경하고,
   * 모든 유저의 상태 또한 나감으로 처리한다.
   * @param roomNo
   */
  public void updateRoomAndUser(int roomNo){
    Map<String,Object> map = new HashMap<>();

    map.put("roomNo", roomNo);
    map.put("roomStatus", "COMPLETED");
    twentyMapper.updateRoomStaus(map);

    map.put("status", "LEFT");
    twentyMapper.updateRoomUserStatus(map);
  }
}
