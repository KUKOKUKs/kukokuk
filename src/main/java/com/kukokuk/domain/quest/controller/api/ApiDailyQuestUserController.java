package com.kukokuk.domain.quest.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.quest.service.DailyQuestUserService;
import com.kukokuk.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/daily-quest-user")
@RequiredArgsConstructor
public class ApiDailyQuestUserController {

    private final DailyQuestUserService dailyQuestUserService;

    // 사용자의 완료된 일일 도전과제 보상 획득 처리 및 획득 후 힌트 개수 요청
    @PutMapping("/{dailyQuestUserNo}/obtain")
    public ResponseEntity<ApiResponse<Integer>> dailyQuestObtainReward(
        @PathVariable("dailyQuestUserNo") int dailyQuestUserNo
        , @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("dailyQuestObtainReward() 컨트롤러 실행");
        int currentHintCount = dailyQuestUserService.updateDailyQuestUserObtained(
            dailyQuestUserNo
            , securityUser.getUser().getUserNo()
        );
        return ResponseEntityUtils.ok(currentHintCount);
    }

}
