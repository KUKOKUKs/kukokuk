package com.kukokuk.domain.twenty.controller.api;

import com.kukokuk.domain.twenty.dto.GameOverDto;
import com.kukokuk.domain.twenty.service.TwentyService;
import com.kukokuk.security.SecurityUser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@Log4j2
@RestController("/api/twenty")
public class ApiTwentyController {
  @Autowired
  private TwentyService twentyService;

  /**
   * 게임 종료 버튼을 누르면, 게임방 상태 + 유저 상태 변경
   * 게임 결과도 입력 해야됨.
   * @param securityUser
   * @param gameOverDto
   */
  @PostMapping("/gameOver")
  public void gameOver(@AuthenticationPrincipal SecurityUser securityUser,
                       @RequestBody GameOverDto gameOverDto) {
    twentyService.updateRoomAndUser(gameOverDto.getRoomNo());

    /* - 게임 결과도 저장하는 서비스 메소드 호출
    * 1. roomNo를 가지고 스무고개 log 테이블에서 정답을 말한 유저No와 총 질문 횟수 값을 조회.
    * 2. 만일 유저No가 null이면 스무고개 테이블에 패배로 등록.
    * 3. 유저No가 null이 아니면, 스무고개 테이블에 승리로 등록 (유저no와 총 질문 횟수 값 넣기)
    */

  }
}
