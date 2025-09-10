package com.kukokuk.domain.twenty.mapper;

import com.kukokuk.domain.twenty.vo.TwentyRoomUser;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TwentyMapper {

  /**
   * roomNo를 가진 게임방 참여자를 조회.
   * => 참여자 명단을 조회할 때 사용.
   * @param roomNo
   * @return
   */
  public List<TwentyRoomUser> getTwentyPlayerList(int roomNo);
}
