package com.kukokuk.domain.history.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.dictation.service.DictationService;
import com.kukokuk.domain.dictation.vo.DictationSession;
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
    private final DictationService dictationService;
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
     * 단계별 퀴즈 최근 이력 조회https://www.notion.so/NHN-2827d704051d80fcb35fc757873bdbb0https://www.notion.so/NHN-2827d704051d80fcb35fc757873bdbb0
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

    /**
     * 받아쓰기 세트 결과 조회
     * @param limit 조회 개수(최대 5개)
     * @param securityUser 사용자
     * @return 받아쓰기 세트 결과
     */
    @GetMapping("/widget/recent/dictation")
    public ResponseEntity<ApiResponse<List<DictationSession>>> getResultDictationHistory(
        @RequestParam(defaultValue = "5") int limit,
        @AuthenticationPrincipal SecurityUser securityUser) {

        int userNo = securityUser.getUser().getUserNo();

        List<DictationSession> dictationHistoryList = dictationService.getResultsSessionsByUserNo(userNo, limit);
        log.info("이력 컴포넌트 조회 성공 - 사용자 번호 : {}, 개수: {}", userNo, limit);

        return ResponseEntityUtils.ok(dictationHistoryList);
    }

}