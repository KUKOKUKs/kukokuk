package com.kukokuk.domain.twenty.mapper;

import com.kukokuk.domain.twenty.dto.RoomUser;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import com.kukokuk.domain.twenty.vo.TwentyRoomUser;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TwentyMapper {

  /**
   * roomNo를 가진 게임방 참여자를 조회.
   * => 참여자 명단을 조회할 때 사용.
   * @param roomNo
   * @return
   */
  public List<RoomUser> getTwentyPlayerList(int roomNo);

  /**
   * 게임방의 참여자의 상태를 변경 -> LEFT or JOINED
   * 입장할 때는, 특정 한 명에 대한 상태만 변경
   * 나갈 때는 특정 한 명 또는 전체에 대한 상태만 변경
   * @param map
   */
  public void updateRoomUserStatus(Map<String, Object> map);

  /**
   * 게임방의 상태를 변경
   */
  public void updateRoomStaus(Map<String, Object> map);


  /**
   * 단일의 게임방을 조회
   * @param roomNo
   * @return
   */
  public TwentyRoom getTwentyRoomByRoomNo(int roomNo);

}
