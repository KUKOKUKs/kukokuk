package com.kukokuk.domain.ranking.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.RankingUtil;
import com.kukokuk.domain.ranking.dto.RankingDto;
import com.kukokuk.domain.ranking.service.RankingService;
import com.kukokuk.domain.ranking.vo.Ranking;
import com.kukokuk.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 랭킹 REST API 컨트롤러
 * 비동기 월별 조회 전용
 *
 * 경로: src/main/java/com/kukokuk/domain/ranking/controller/api/ApiRankingController.java
 */
@Log4j2
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class ApiRankingController {

    private final RankingService rankingService;
    private static final int RANKING_LIMIT = 50;
    private static final int HISTORY_LIMIT = 6;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 특정 월의 특정 컨텐츠 랭킹 조회 (비동기용)
     * @param contentType 컨텐츠 타입 (SPEED, LEVEL, DICTATION)
     * @param rankMonth 조회할 월 (YYYY-MM)
     * @param securityUser 현재 사용자 정보
     * @return 랭킹 데이터
     */
    @GetMapping("/month")
    public ResponseEntity<ApiResponse<RankingMonthlyData>> getRankingByMonth(
        @RequestParam String contentType,
        @RequestParam String rankMonth,
        @AuthenticationPrincipal SecurityUser securityUser) {

        int userNo = securityUser.getUser().getUserNo();
        log.info("getRankingByMonth API 호출 - userNo: {}, contentType: {}, rankMonth: {}",
            userNo, contentType, rankMonth);

        // 날짜 유효성 검증
        if (!RankingUtil.isValidMonth(rankMonth)) {
            log.warn("유효하지 않은 날짜 형식: {}", rankMonth);
            ApiResponse<RankingMonthlyData> errorResponse =
                new ApiResponse<>(false, 400, "유효하지 않은 날짜 형식입니다. (YYYY-MM)", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 미래 날짜 검증
        if (RankingUtil.isFutureMonth(rankMonth)) {
            log.warn("미래 날짜 요청: {}", rankMonth);
            ApiResponse<RankingMonthlyData> errorResponse =
                new ApiResponse<>(false, 400, "미래 날짜의 랭킹은 조회할 수 없습니다.", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            boolean isCurrentMonth = RankingUtil.isCurrentMonth(rankMonth);

            // 3개 타입 모두 조회
            List<RankingDto> speedQuizRank;
            List<RankingDto> levelQuizRank;
            List<RankingDto> dictationRank;

            if (isCurrentMonth) {
                speedQuizRank = rankingService.getGlobalRankingDtos("SPEED", RANKING_LIMIT);
                levelQuizRank = rankingService.getGlobalRankingDtos("LEVEL", RANKING_LIMIT);
                dictationRank = rankingService.getGlobalRankingDtos("DICTATION", RANKING_LIMIT);
            } else {
                speedQuizRank = rankingService.getGlobalRankingDtosByMonth("SPEED", rankMonth, RANKING_LIMIT);
                levelQuizRank = rankingService.getGlobalRankingDtosByMonth("LEVEL", rankMonth, RANKING_LIMIT);
                dictationRank = rankingService.getGlobalRankingDtosByMonth("DICTATION", rankMonth, RANKING_LIMIT);
            }

            // 현재 선택된 contentType의 랭킹
            List<RankingDto> globalRankings = selectRankingByContentType(
                contentType, speedQuizRank, levelQuizRank, dictationRank);

            // 내 랭킹 정보
            Ranking myRankingInfo = null;
            Integer myRanking = null;

            try {
                if (isCurrentMonth) {
                    myRankingInfo = rankingService.getRankingByUserAndContent(userNo, contentType);
                    myRanking = rankingService.getGlobalUserRanking(userNo, contentType);
                } else {
                    myRankingInfo = rankingService.getRankingByUserContentAndMonth(userNo, contentType, rankMonth);
                    myRanking = rankingService.getGlobalUserRankingByMonth(userNo, contentType, rankMonth);
                }
            } catch (Exception e) {
                log.debug("사용자 랭킹 정보 없음 - userNo: {}, contentType: {}, rankMonth: {}",
                    userNo, contentType, rankMonth);
            }

            // 내 월별 히스토리
            List<Ranking> myHistory = rankingService.getUserMonthlyHistory(userNo, contentType, HISTORY_LIMIT);

            // 월 네비게이션
            YearMonth currentYearMonth = YearMonth.parse(rankMonth, MONTH_FORMATTER);
            String prevMonth = currentYearMonth.minusMonths(1).format(MONTH_FORMATTER);
            String nextMonth = null;

            YearMonth nextYearMonth = currentYearMonth.plusMonths(1);
            if (!nextYearMonth.isAfter(YearMonth.now())) {
                nextMonth = nextYearMonth.format(MONTH_FORMATTER);
            }

            // 응답 데이터 생성
            RankingMonthlyData data = new RankingMonthlyData(
                speedQuizRank,
                levelQuizRank,
                dictationRank,
                globalRankings,
                myRankingInfo,
                myRanking,
                myHistory,
                rankMonth,
                prevMonth,
                nextMonth,
                isCurrentMonth
            );

            log.info("월별 랭킹 조회 완료 - 월: {}, contentType: {}, 데이터: {}개",
                rankMonth, contentType, globalRankings.size());

            return ResponseEntity.ok(ApiResponse.success(data));

        } catch (Exception e) {
            log.error("월별 랭킹 조회 실패 - contentType: {}, rankMonth: {}", contentType, rankMonth, e);
            ApiResponse<RankingMonthlyData> errorResponse =
                new ApiResponse<>(false, 500, "랭킹 조회 중 오류가 발생했습니다.", null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * contentType에 따라 적절한 랭킹 리스트 선택
     */
    private List<RankingDto> selectRankingByContentType(
        String contentType,
        List<RankingDto> speedRank,
        List<RankingDto> levelRank,
        List<RankingDto> dictationRank) {

        switch (contentType.toUpperCase()) {
            case "LEVEL":
                return levelRank;
            case "DICTATION":
                return dictationRank;
            case "SPEED":
            default:
                return speedRank;
        }
    }

    /**
     * 월별 랭킹 데이터를 담는 응답 클래스
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class RankingMonthlyData {
        private List<RankingDto> speedQuizRank;
        private List<RankingDto> levelQuizRank;
        private List<RankingDto> dictationRank;
        private List<RankingDto> globalRankings;
        private Ranking myRankingInfo;
        private Integer myRanking;
        private List<Ranking> myHistory;
        private String rankMonth;
        private String prevMonth;
        private String nextMonth;
        private boolean isCurrentMonth;
    }
}