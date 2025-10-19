package com.kukokuk.domain.quest.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.quest.dto.DailyQuestStatusDto;
import com.kukokuk.domain.quest.service.DailyQuestService;
import com.kukokuk.domain.quest.vo.DailyQuest;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/daily-quests")
@RequiredArgsConstructor
public class ApiDailyQuestController {

    private final DailyQuestService dailyQuestService;

    // 사용자 번호로 모든 퀘스트와 진행도 및 보상 획득여부 정보를 포함한 목록 조회 요청
    @GetMapping
    public ResponseEntity<ApiResponse<List<DailyQuestStatusDto>>> dailyQuestList(
        @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("ApiDailyQuestController dailyQuestList() 컨트롤러 실행");
        return ResponseEntityUtils.ok(
            dailyQuestService.getDailyQuestsStatus(securityUser.getUser().getUserNo())
        );
    }

    // 미인증 시 사용될 모든 퀘스트 목록 조회 요청
    @GetMapping("/basic")
    public ResponseEntity<ApiResponse<List<DailyQuest>>> dailyQuestBasic() {
        log.info("ApiDailyQuestController dailyQuestBasic() 컨트롤러 실행");
        return ResponseEntityUtils.ok(
            dailyQuestService.getDailyQuests()
        );
    }

}