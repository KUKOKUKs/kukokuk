package com.kukokuk.domain.group.controller.view;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

    // 그룹 메인 페이지
    @GetMapping
    public String groupPage() {
        log.info("dailyQuests() 컨트롤러 실행");
        return "group/main";
    }

}
