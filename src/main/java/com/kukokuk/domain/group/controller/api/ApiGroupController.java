package com.kukokuk.domain.group.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.dto.Page;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.group.service.GroupService;
import com.kukokuk.domain.group.vo.Group;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class ApiGroupController {

    private final GroupService groupService;

    // 그룹 검색(페이지네이션)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Group>>> searchGroups(
        @RequestParam(defaultValue = "1") int page
        , @RequestParam String keyword) {
        log.info("ApiGroupController searchGroups() 컨트롤러 실행");

        Map<String, Object> condition = new HashMap<>();
        condition.put("keyword", keyword);

        return ResponseEntityUtils.ok(
            groupService.getGroups(page, condition)
        );
    }

}
