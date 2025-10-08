package com.kukokuk.domain.rank.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.domain.rank.dto.RankingWidgetDto;
import com.kukokuk.domain.rank.dto.RanksResponseDto;
import com.kukokuk.domain.rank.service.RankService;
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

/**
 * 랭킹 위젯용 API 컨트롤러
 *
 * 경로: src/main/java/com/kukokuk/domain/ranking/controller/api/ApiRankingWidgetController.java
 */
@Log4j2
@RestController
@RequestMapping("/api/ranking/widget")
@RequiredArgsConstructor
public class ApiRankingWidgetController {

    private final RankService rankingService;
    private static final int WIDGET_LIMIT = 5;

    /**
     * 랭킹 위젯 요약 정보 조회 (스피드퀴즈 + 단계별퀴즈 + 받아쓰기)
     * @param securityUser 현재 사용자 정보
     * @return 위젯용 랭킹 요약 데이터
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<RankingWidgetDto>> getRankingWidgetSummary(
        @AuthenticationPrincipal SecurityUser securityUser) {

        int userNo = securityUser.getUser().getUserNo();
        log.info("getRankingWidgetSummary API 호출 - userNo: {}", userNo);

        try {
            RankingWidgetDto widgetData = new RankingWidgetDto();

            List<RanksResponseDto> speedRankings = rankingService.getGlobalRankingDtos("SPEED", WIDGET_LIMIT);
            widgetData.setSpeedRankings(speedRankings);

            List<RanksResponseDto> stepRankings = rankingService.getGlobalRankingDtos("LEVEL", WIDGET_LIMIT);
            widgetData.setStepRankings(stepRankings);

            List<RanksResponseDto> dictationRankings = rankingService.getGlobalRankingDtos("DICTATION", WIDGET_LIMIT);
            widgetData.setDictationRankings(dictationRankings);

            try {
                int mySpeedRank = rankingService.getGlobalUserRanking(userNo, "SPEED");
                widgetData.setMySpeedRank(mySpeedRank);
            } catch (Exception e) {
                log.debug("사용자 스피드퀴즈 순위 없음 - userNo: {}", userNo);
                widgetData.setMySpeedRank(null);
            }

            try {
                int myStepRank = rankingService.getGlobalUserRanking(userNo, "LEVEL");
                widgetData.setMyStepRank(myStepRank);
            } catch (Exception e) {
                log.debug("사용자 단계별퀴즈 순위 없음 - userNo: {}", userNo);
                widgetData.setMyStepRank(null);
            }

            try {
                int myDictationRank = rankingService.getGlobalUserRanking(userNo, "DICTATION");
                widgetData.setMyDictationRank(myDictationRank);
            } catch (Exception e) {
                log.debug("사용자 받아쓰기 순위 없음 - userNo: {}", userNo);
                widgetData.setMyDictationRank(null);
            }

            return ResponseEntity.ok(ApiResponse.success(widgetData));

        } catch (Exception e) {
            log.error("랭킹 위젯 요약 조회 실패 - userNo: {}", userNo, e);
            ApiResponse<RankingWidgetDto> errorResponse =
                new ApiResponse<>(false, 500, "랭킹 위젯 데이터 조회에 실패했습니다.", null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 특정 컨텐츠의 상위 랭킹 조회 (위젯용)
     * @param contentType 컨텐츠 타입
     * @param limit 조회할 개수 (기본 5개)
     * @return 상위 랭킹 목록
     */
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<RanksResponseDto>>> getTopRankingsForWidget(
        @RequestParam String contentType,
        @RequestParam(defaultValue = "5") int limit) {

        log.info("getTopRankingsForWidget API 호출 - contentType: {}, limit: {}", contentType, limit);

        try {
            List<RanksResponseDto> rankings = rankingService.getGlobalRankingDtos(contentType, limit);
            return ResponseEntity.ok(ApiResponse.success(rankings));
        } catch (Exception e) {
            log.error("위젯용 상위 랭킹 조회 실패 - contentType: {}, limit: {}", contentType, limit, e);
            ApiResponse<List<RanksResponseDto>> errorResponse =
                new ApiResponse<>(false, 500, "랭킹 조회에 실패했습니다.", null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 현재 사용자의 특정 컨텐츠 순위 조회 (위젯용)
     * @param contentType 컨텐츠 타입
     * @param securityUser 현재 사용자 정보
     * @return 사용자 순위
     */
    @GetMapping("/my-rank")
    public ResponseEntity<ApiResponse<Integer>> getMyRankForWidget(
        @RequestParam String contentType,
        @AuthenticationPrincipal SecurityUser securityUser) {

        int userNo = securityUser.getUser().getUserNo();
        log.info("getMyRankForWidget API 호출 - userNo: {}, contentType: {}", userNo, contentType);

        try {
            int ranking = rankingService.getGlobalUserRanking(userNo, contentType);
            return ResponseEntity.ok(ApiResponse.success(ranking));
        } catch (Exception e) {
            log.debug("위젯용 사용자 순위 없음 - userNo: {}, contentType: {}", userNo, contentType);
            return ResponseEntity.ok(ApiResponse.success("아직 순위가 없습니다.", (Integer) null));
        }
    }
}