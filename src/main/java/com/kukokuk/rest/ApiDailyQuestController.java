package com.kukokuk.rest;

import com.kukokuk.response.ApiResponse;
import com.kukokuk.response.ResponseEntityUtils;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.DailyQuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-quests")
@RequiredArgsConstructor
public class ApiDailyQuestController {

    private final DailyQuestService dailyQuestService;

    @GetMapping();
    public ResponseEntity<ApiResponse<>> questList() {

    }

    /**
     * 사용자가 도전과제를 완료한 후 힌트 아이템 버튼을 클릭시,
     * 도전과제의 보상(힌트)을 획득하는 API
     * 즉, daily_quest_users 테이블의 IS_OBTAINED를 “Y”로 변경하는 API
     * @param dailyQuestUserNo 변경할 도전과제-사용자 이력번호
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{dailyQuestUserNo}")
    public ResponseEntity<ApiResponse<Void>> updateDailyQuestUserIsObtained(
        @PathVariable("dailyQuestUserNo") int dailyQuestUserNo,
        @AuthenticationPrincipal SecurityUser securityUser) {

        // 해당 일일도전과제 수행 정보의 IS_OBTAINED 컬럼을 "Y"로 변경하는 서비스 메소드 호출
        dailyQuestService.updateDailyQuestUser(dailyQuestUserNo,
            securityUser.getUser().getUserNo());

        return ResponseEntityUtils.ok("사용자 아이템 획득 정보 변경 완료");
    }
}