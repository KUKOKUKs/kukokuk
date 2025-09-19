package com.kukokuk.domain.ranking.controller.view;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 랭킹 페이지 뷰 컨트롤러
 */
@Log4j2
@Controller
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 랭킹 메인 페이지
     * @param contentType 컨텐츠 타입 (기본: SPEED)
     * @param rankMonth 조회할 월 (기본: 현재 월)
     * @param limit 조회할 개수 (기본: 50)
     * @param model 뷰 모델
     * @param securityUser 현재 사용자 정보
     * @return 랭킹 페이지 템플릿
     */
    @GetMapping
    public String rankingMainPage(
        @RequestParam(defaultValue = "SPEED") String contentType,
        @RequestParam(required = false) String rankMonth,
        @RequestParam(defaultValue = "50") int limit,
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser) {

        int userNo = securityUser.getUser().getUserNo();
        log.info("rankingMainPage 호출 - userNo: {}, contentType: {}, month: {}, limit: {}",
            userNo, contentType, rankMonth, limit);

        try {
            // 현재 월 설정
            String currentMonth = getCurrentMonth();
            if (rankMonth == null || rankMonth.trim().isEmpty()) {
                rankMonth = currentMonth;
            }

            // 전체 랭킹 조회
            List<Ranking> globalRankings;
            if (currentMonth.equals(rankMonth)) {
                globalRankings = rankingService.getGlobalRankings(contentType, limit);
            } else {
                globalRankings = rankingService.getGlobalRankingsByMonth(contentType, rankMonth, limit);
            }

            // 내 순위 조회
            Integer myRanking = null;
            Ranking myRankingInfo = null;
            try {
                if (currentMonth.equals(rankMonth)) {
                    myRanking = rankingService.getGlobalUserRanking(userNo, contentType);
                    myRankingInfo = rankingService.getRankingByUserAndContent(userNo, contentType);
                } else {
                    myRanking = rankingService.getGlobalUserRankingByMonth(userNo, contentType, rankMonth);
                    myRankingInfo = rankingService.getRankingByUserContentAndMonth(userNo, contentType, rankMonth);
                }
            } catch (Exception e) {
                log.debug("사용자 순위 정보 없음 - userNo: {}, contentType: {}, month: {}",
                    userNo, contentType, rankMonth);
            }

            // 사용자 월별 히스토리 (최근 6개월)
            List<Ranking> myHistory = rankingService.getUserMonthlyHistory(userNo, contentType, 6);

            // 모델에 데이터 추가
            model.addAttribute("contentType", contentType);
            model.addAttribute("rankMonth", rankMonth);
            model.addAttribute("currentMonth", currentMonth);
            model.addAttribute("limit", limit);
            model.addAttribute("globalRankings", globalRankings);
            model.addAttribute("myRanking", myRanking);
            model.addAttribute("myRankingInfo", myRankingInfo);
            model.addAttribute("myHistory", myHistory);
            model.addAttribute("isCurrentMonth", currentMonth.equals(rankMonth));

            // 컨텐츠 타입별 한글명
            model.addAttribute("contentTypeName", getContentTypeName(contentType));

            // 이전/다음 월 계산
            model.addAttribute("prevMonth", getPrevMonth(rankMonth));
            model.addAttribute("nextMonth", getNextMonth(rankMonth, currentMonth));

            return "ranking/main";

        } catch (Exception e) {
            log.error("랭킹 페이지 로드 실패 - userNo: {}, contentType: {}, month: {}",
                userNo, contentType, rankMonth, e);
            model.addAttribute("errorMessage", "랭킹 데이터를 불러올 수 없습니다.");
            return "ranking/main";
        }
    }

    /**
     * 그룹 랭킹 페이지
     * @param groupNo 그룹 번호
     * @param contentType 컨텐츠 타입 (기본: SPEED)
     * @param rankMonth 조회할 월 (기본: 현재 월)
     * @param limit 조회할 개수 (기본: 30)
     * @param model 뷰 모델
     * @param securityUser 현재 사용자 정보
     * @return 그룹 랭킹 페이지 템플릿
     */
    @GetMapping("/group")
    public String groupRankingPage(
        @RequestParam int groupNo,
        @RequestParam(defaultValue = "SPEED") String contentType,
        @RequestParam(required = false) String rankMonth,
        @RequestParam(defaultValue = "30") int limit,
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser) {

        int userNo = securityUser.getUser().getUserNo();
        log.info("groupRankingPage 호출 - userNo: {}, groupNo: {}, contentType: {}, month: {}, limit: {}",
            userNo, groupNo, contentType, rankMonth, limit);

        try {
            // 현재 월 설정
            String currentMonth = getCurrentMonth();
            if (rankMonth == null || rankMonth.trim().isEmpty()) {
                rankMonth = currentMonth;
            }

            // 그룹 랭킹 조회
            List<Ranking> groupRankings;
            if (currentMonth.equals(rankMonth)) {
                groupRankings = rankingService.getGroupRankings(contentType, groupNo, limit);
            } else {
                groupRankings = rankingService.getGroupRankingsByMonth(contentType, groupNo, rankMonth, limit);
            }

            // 그룹 내 내 순위 조회
            Integer myGroupRanking = null;
            try {
                if (currentMonth.equals(rankMonth)) {
                    myGroupRanking = rankingService.getGroupUserRanking(userNo, contentType, groupNo);
                } else {
                    myGroupRanking = rankingService.getGroupUserRankingByMonth(userNo, contentType, groupNo, rankMonth);
                }
            } catch (Exception e) {
                log.debug("사용자 그룹 순위 정보 없음 - userNo: {}, groupNo: {}, contentType: {}, month: {}",
                    userNo, groupNo, contentType, rankMonth);
            }

            // 모델에 데이터 추가
            model.addAttribute("groupNo", groupNo);
            model.addAttribute("contentType", contentType);
            model.addAttribute("rankMonth", rankMonth);
            model.addAttribute("currentMonth", currentMonth);
            model.addAttribute("limit", limit);
            model.addAttribute("groupRankings", groupRankings);
            model.addAttribute("myGroupRanking", myGroupRanking);
            model.addAttribute("isCurrentMonth", currentMonth.equals(rankMonth));
            model.addAttribute("contentTypeName", getContentTypeName(contentType));
            model.addAttribute("prevMonth", getPrevMonth(rankMonth));
            model.addAttribute("nextMonth", getNextMonth(rankMonth, currentMonth));

            return "ranking/group";

        } catch (Exception e) {
            log.error("그룹 랭킹 페이지 로드 실패 - userNo: {}, groupNo: {}, contentType: {}, month: {}",
                userNo, groupNo, contentType, rankMonth, e);
            model.addAttribute("errorMessage", "그룹 랭킹 데이터를 불러올 수 없습니다.");
            return "ranking/group";
        }
    }

    /**
     * 현재 년월(YYYY-MM) 반환
     */
    private String getCurrentMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /**
     * 컨텐츠 타입별 한글명 반환
     */
    private String getContentTypeName(String contentType) {
        switch (contentType.toUpperCase()) {
            case "SPEED": return "스피드퀴즈";
            case "LEVEL": return "레벨퀴즈";
            case "DICTATION": return "받아쓰기";
            case "TWENTY": return "스무고개";
            case "STUDY": return "학습";
            case "ESSAY": return "서술형";
            default: return contentType;
        }
    }

    /**
     * 이전 월 계산 (YYYY-MM)
     */
    private String getPrevMonth(String currentMonth) {
        try {
            LocalDate date = LocalDate.parse(currentMonth + "-01");
            return date.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (Exception e) {
            return getCurrentMonth();
        }
    }

    /**
     * 다음 월 계산 (현재 월을 넘지 않음)
     */
    private String getNextMonth(String currentMonth, String systemCurrentMonth) {
        try {
            LocalDate date = LocalDate.parse(currentMonth + "-01");
            String nextMonth = date.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

            // 현재 월을 넘지 않도록 제한
            if (nextMonth.compareTo(systemCurrentMonth) > 0) {
                return null;
            }
            return nextMonth;
        } catch (Exception e) {
            return null;
        }
    }
}