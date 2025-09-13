package com.kukokuk.domain.group.controller.view;

import com.kukokuk.domain.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    // 그룹 메인 페이지
    @GetMapping
    public String groupPage(Model model) {
        log.info("GroupController groupPage() 컨트롤러 실행");
        model.addAttribute("randomGroups", groupService.getRandomGroups(10));
        return "group/main";
    }

}
