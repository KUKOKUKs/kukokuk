package com.kukokuk.domain.ranking.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.domain.ranking.dto.RankingWidgetDto;
import com.kukokuk.domain.ranking.service.RankingService;
import com.kukokuk.domain.ranking.vo.Ranking;
import com.kukokuk.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 랭킹 위젯용 API 컨트롤러
 */
@Log4j2
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class ApiRankingController {

    private final RankingService rankingService;

    /**
     * 랭킹 위젯 요약 정보 조회 (스피드퀴즈 + 받아쓰기)
     * @param securityUser 현재 사용자 정보
     * @return 위젯용 랭킹 요약 데이터
     */
    @GetMapping("/widget/summary")
    public ResponseEntity<ApiResponse<RankingWidgetDto>> getRankingWidgetSummary(
        @AuthenticationPrincipal SecurityUser securityUser) {

        int userNo = securityUser.getUser().getUserNo();
        log.info("getRankingWidgetSummary API 호출 - userNo: {}", userNo);

        try {
            RankingWidgetDto widgetData = new RankingWidgetDto();

            // 스피드퀴즈 TOP 5 랭킹
            List<Ranking> speedRankings = rankingService.getGlobalRankings("SPEED", 5);
            widgetData.setSpeedRankings(speedRankings);

            // 받아쓰기 TOP 5 랭킹
            List<Ranking> dictationRankings = rankingService.getGlobalRankings("DICTATION", 5);
            widgetData.setDictationRankings(dictationRankings);

            // 내 스피드퀴즈 순위
            try {
                int mySpeedRank = rankingService.getGlobalUserRanking(userNo, "SPEED");
                widgetData.setMySpeedRank(mySpeedRank);
            } catch (Exception e) {
                log.debug("사용자 스피드퀴즈 순위 없음 - userNo: {}", userNo);
                widgetData.setMySpeedRank(null);
            }

            // 내 받아쓰기 순위
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
            ApiResponse<RankingWidgetDto> errorResponse = new ApiResponse<>(false, 400, "랭킹 위젯 데이터 조회에 실패했습니다.", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 특정 컨텐츠의 상위 랭킹 조회 (위젯용)
     * @param contentType 컨텐츠 타입
     * @param limit 조회할 개수 (기본 5개)
     * @return 상위 랭킹 목록
     */
    @GetMapping("/widget/top")
    public ResponseEntity<ApiResponse<List<Ranking>>> getTopRankingsForWidget(
        @RequestParam String contentType,
        @RequestParam(defaultValue = "5") int limit) {

        log.info("getTopRankingsForWidget API 호출 - contentType: {}, limit: {}", contentType, limit);

        try {
            List<Ranking> rankings = rankingService.getGlobalRankings(contentType, limit);
            return ResponseEntity.ok(ApiResponse.success(rankings));
        } catch (Exception e) {
            log.error("위젯용 상위 랭킹 조회 실패 - contentType: {}, limit: {}", contentType, limit, e);
            ApiResponse<List<Ranking>> errorResponse = new ApiResponse<>(false, 400, "랭킹 조회에 실패했습니다.", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 현재 사용자의 특정 컨텐츠 순위 조회 (위젯용)
     * @param contentType 컨텐츠 타입
     * @param securityUser 현재 사용자 정보
     * @return 사용자 순위
     */
    @GetMapping("/widget/my-rank")
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