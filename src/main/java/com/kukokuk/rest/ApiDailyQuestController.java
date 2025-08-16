package com.kukokuk.rest;

import com.kukokuk.response.ApiResponse;
import com.kukokuk.response.DailyQuestStatusResponse;
import com.kukokuk.response.ResponseEntityUtils;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.DailyQuestService;
import com.kukokuk.vo.DailyQuest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/daily-quests")
@RequiredArgsConstructor
public class ApiDailyQuestController {

    private final DailyQuestService dailyQuestService;

    // 사용자의 완료된 일일 도전과제 보상 획득 처리 및 획득 후 힌트 개수 요청
    @PutMapping("/{dailyQuestUserNo}/obtain")
    public ResponseEntity<ApiResponse<Integer>> dailyQuestObtainReward(
        @PathVariable("dailyQuestUserNo") int dailyQuestUserNo
        , @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("dailyQuestGetHint() 컨트롤러 실행");
        int currentHintCount = dailyQuestService.updateDailyQuestUserObtained(
            dailyQuestUserNo
            , securityUser.getUser().getUserNo()
        );
        return ResponseEntityUtils.ok(currentHintCount);
    }

    // 미인증 시 사용될 모든 퀘스트 목록 조회 요청
    @GetMapping("/basic")
    public ResponseEntity<ApiResponse<List<DailyQuest>>> dailyQuestBasic() {
        log.info("dailyQuestBasic() 컨트롤러 실행");
        return ResponseEntityUtils.ok(
            dailyQuestService.getDailyQuests()
        );
    }

    // 사용자 번호로 모든 퀘스트와 진행도 및 보상 획득여부 정보를 포함한 목록 조회 요청
    @GetMapping
    public ResponseEntity<ApiResponse<List<DailyQuestStatusResponse>>> dailyQuestList(
        @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("dailyQuestList() 컨트롤러 실행");
        return ResponseEntityUtils.ok(
            dailyQuestService.getDailyQuestsStatus(securityUser.getUser().getUserNo())
        );
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