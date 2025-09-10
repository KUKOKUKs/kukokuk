package com.kukokuk.domain.twenty.service;

import com.kukokuk.domain.twenty.mapper.TwentyMapper;
import com.kukokuk.domain.twenty.vo.TwentyRoomUser;
import com.kukokuk.domain.user.vo.User;
import java.util.List;
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
  public List<TwentyRoomUser> getTwentyPlayerList(int roomNo) {
    return twentyMapper.getTwentyPlayerList(roomNo);
  }
}
