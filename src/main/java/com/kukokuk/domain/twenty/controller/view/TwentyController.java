package com.kukokuk.domain.twenty.controller.view;

import com.kukokuk.domain.twenty.service.TwentyService;
import com.kukokuk.domain.twenty.vo.TwentyRoomUser;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@RequestMapping("/twenty")
public class TwentyController {
  @Autowired
  private TwentyService twentyService;

  // 학생들의 경우, 이 경로로 게임방을 이동!
  @GetMapping("/gameRoom/{roomId}")
  public String gameRoom(@PathVariable int roomId,
                         Model model,
                         @AuthenticationPrincipal SecurityUser securityUser) {
    model.addAttribute("roomId",1);

    List<TwentyRoomUser> list = twentyService.getTwentyPlayerList(roomId);
    model.addAttribute("list", list);

    int userNo = securityUser.getUser().getUserNo();
    model.addAttribute("userNo",userNo);
    return "twenty/gameRoom";
  }

  //교사의 경우, 설정을 마치고 바로 게임방으로 이동!, PostMapping을 해야된다.

}
