package com.kukokuk.domain.group.controller.api;

import com.kukokuk.common.constant.PaginationEnum;
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

    /**
     * 그룹 검색(페이지네이션)
     * <p>
     * keyword 유무에 따라 랜덤리스트(기본값)/검색 리스트 전달
     * @param page 조회할 페이지 번호 (기본값 1)
     * @param keyword 검색어
     * @return 그룹 목록 정보, 페이지네이션(랜덤리스트일 경우 null)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Group>>> searchGroups(
        @RequestParam(defaultValue = "1") int page
        , @RequestParam(required = false) String keyword) {
        log.info("ApiGroupController searchGroups() 컨트롤러 실행 page: {}, keyword: {}", page, keyword);

        if (keyword == null || keyword.isBlank()) {
            // 검색어 없으면 랜덤 그룹 보여주기
            log.info("ApiGroupController getRandomGroups() 랜덤 그룹 목록 조회(기본값)");

            // 템플릿 구조를 통일하기 위해 Page객체에 담기
            Page<Group> randomGroups = new Page<>();
            randomGroups.setItems(groupService.getRandomGroups(PaginationEnum.DEFAULT_ROWS));
            return ResponseEntityUtils.ok(randomGroups);
        } else {
            // 검색어 있으면 검색 결과 보여주기
            log.info("ApiGroupController getGroups() 검색 그룹 목록 조회");
            Map<String, Object> condition = new HashMap<>();
            condition.put("keyword", keyword);
            return ResponseEntityUtils.ok(groupService.getGroups(page, condition));
        }
    }

}
