package com.kukokuk.domain.ranking.controller.view;

import com.kukokuk.common.util.RankingUtil;
import com.kukokuk.domain.ranking.dto.RankingDto;
import com.kukokuk.domain.ranking.service.RankingService;
import com.kukokuk.domain.ranking.vo.Ranking;
import com.kukokuk.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 랭킹 페이지 뷰 컨트롤러
 *
 */
@Log4j2
@Controller
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;
    private static final int RANKING_LIMIT = 50;
    private static final int HISTORY_LIMIT = 6;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 랭킹 메인 페이지
     * @param contentType 컨텐츠 타입 (기본값: SPEED)
     * @param rankMonth 조회할 월 (기본값: 현재 월)
     * @param model 뷰 모델
     * @param securityUser 현재 사용자 정보
     * @return 랭킹 페이지 템플릿
     */
    @GetMapping
    public String rankingMainPage(
        @RequestParam(defaultValue = "SPEED") String contentType,
        @RequestParam(required = false) String rankMonth,
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser) {

        int userNo = securityUser.getUser().getUserNo();

        // rankMonth가 없으면 당월 사용
        if (rankMonth == null || rankMonth.trim().isEmpty()) {
            rankMonth = RankingUtil.getCurrentMonth();
        }

        log.info("rankingMainPage 호출 - userNo: {}, contentType: {}, rankMonth: {}",
            userNo, contentType, rankMonth);

        // 날짜 유효성 검증
        if (!RankingUtil.isValidMonth(rankMonth)) {
            log.warn("유효하지 않은 날짜 형식: {}", rankMonth);
            rankMonth = RankingUtil.getCurrentMonth();
        }

        // 현재 월 여부
        boolean isCurrentMonth = RankingUtil.isCurrentMonth(rankMonth);

        // 3개 컨텐츠 타입 모두 조회 (데이터가 적으므로 한 번에 전달)
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

        // 현재 선택된 contentType의 한글명
        String contentTypeName = getContentTypeName(contentType);

        // 내 랭킹 정보 조회
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

        // 내 월별 히스토리 (현재 contentType 기준)
        List<Ranking> myHistory = rankingService.getUserMonthlyHistory(userNo, contentType, HISTORY_LIMIT);

        // 월 네비게이션 (이전 월, 다음 월)
        YearMonth currentYearMonth = YearMonth.parse(rankMonth, MONTH_FORMATTER);
        String prevMonth = currentYearMonth.minusMonths(1).format(MONTH_FORMATTER);
        String nextMonth = null;

        // 다음 월은 현재 월을 넘지 않도록
        YearMonth nextYearMonth = currentYearMonth.plusMonths(1);
        if (!nextYearMonth.isAfter(YearMonth.now())) {
            nextMonth = nextYearMonth.format(MONTH_FORMATTER);
        }

        // Model에 데이터 담기
        model.addAttribute("contentType", contentType);
        model.addAttribute("contentTypeName", contentTypeName);
        model.addAttribute("rankMonth", rankMonth);
        model.addAttribute("isCurrentMonth", isCurrentMonth);
        model.addAttribute("prevMonth", prevMonth);
        model.addAttribute("nextMonth", nextMonth);

        // 3개 타입 랭킹 모두 전달
        model.addAttribute("speedQuizRank", speedQuizRank);
        model.addAttribute("levelQuizRank", levelQuizRank);
        model.addAttribute("dictationRank", dictationRank);

        // Thymeleaf에서 contentType에 따라 선택할 수 있도록 전달
        // globalRankings는 현재 선택된 contentType의 랭킹
        List<RankingDto> globalRankings = selectRankingByContentType(
            contentType, speedQuizRank, levelQuizRank, dictationRank);
        model.addAttribute("globalRankings", globalRankings);

        // 내 정보
        model.addAttribute("myRankingInfo", myRankingInfo);
        model.addAttribute("myRanking", myRanking);
        model.addAttribute("myHistory", myHistory);

        log.info("랭킹 데이터 전달 완료 - 스피드: {}, 레벨: {}, 받아쓰기: {}, 선택: {}",
            speedQuizRank.size(), levelQuizRank.size(), dictationRank.size(), contentType);

        return "ranking/main";
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
     * 컨텐츠 타입 한글명 반환
     */
    private String getContentTypeName(String contentType) {
        switch (contentType.toUpperCase()) {
            case "SPEED":
                return "스피드퀴즈";
            case "LEVEL":
                return "단계별퀴즈";
            case "DICTATION":
                return "받아쓰기";
            default:
                return "스피드퀴즈";
        }
    }
}