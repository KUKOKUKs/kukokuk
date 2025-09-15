package com.kukokuk.domain.group.controller.view;

import com.kukokuk.common.constant.PaginationEnum;
import com.kukokuk.common.dto.Page;
import com.kukokuk.domain.group.service.GroupService;
import com.kukokuk.domain.group.vo.Group;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    /**
     * 그룹 메인 페이지
     * <p>
     * 검색 후(비동기 요청) 새로고침 시에도 유지되도록
     * keyword 유무에 따라 랜덤리스트(기본값)/검색 리스트 전달
     */
    @GetMapping
    public String groupPage(
        @RequestParam(defaultValue = "1") int page
        , @RequestParam(required = false) String keyword
        , Model model) {
        log.info("GroupController groupPage() 컨트롤러 실행");

        if (keyword == null || keyword.isBlank()) {
            // 검색어 없으면 랜덤 그룹 보여주기
            log.info("GroupController groupPage() 랜덤 그룹 목록 조회(기본값)");
            
            // 템플릿 구조를 통일하기 위해 Page객체에 담기
            Page<Group> randomGroups = new Page<>();
            randomGroups.setItems(groupService.getRandomGroups(PaginationEnum.DEFAULT_ROWS));
            model.addAttribute("groups", randomGroups);
        } else {
            // 검색어 있으면 검색 결과 보여주기
            log.info("GroupController groupPage() 검색 그룹 목록 조회(비동기 요청 이후 새로고침)");
            Map<String, Object> condition = new HashMap<>();
            condition.put("keyword", keyword);
            model.addAttribute("groups", groupService.getGroups(page, condition));
        }

        return "group/main";
    }

}
