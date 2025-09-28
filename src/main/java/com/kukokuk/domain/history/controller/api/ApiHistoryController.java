package com.kukokuk.domain.history.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.history.dto.GameHistoryDto;
import com.kukokuk.domain.history.service.GameHistoryService;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class ApiHistoryController {

    private final GameHistoryService gameHistoryService;
    /**
     * 스피드 퀴즈 최근 이력 조회
     */
    @GetMapping("/widget/recent/speed")
    public ResponseEntity<ApiResponse<List<GameHistoryDto>>> getRecentSpeedHistory(
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(defaultValue = "5") int limit) {

        int userNo = securityUser.getUser().getUserNo();
        log.info("[API] 스피드 퀴즈 이력 조회 - userNo: {}, limit: {}", userNo, limit);

        List<GameHistoryDto> historyList = gameHistoryService.getRecentSpeedHistory(userNo, limit);
        return ResponseEntityUtils.ok(historyList);
    }

    /**
     * 단계별 퀴즈 최근 이력 조회
     */
    @GetMapping("/widget/recent/level")
    public ResponseEntity<ApiResponse<List<GameHistoryDto>>> getRecentLevelHistory(
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(defaultValue = "5") int limit) {

        int userNo = securityUser.getUser().getUserNo();
        log.info("[API] 단계별 퀴즈 이력 조회 - userNo: {}, limit: {}", userNo, limit);

        List<GameHistoryDto> historyList = gameHistoryService.getRecentLevelHistory(userNo, limit);
        return ResponseEntityUtils.ok(historyList);
    }


}