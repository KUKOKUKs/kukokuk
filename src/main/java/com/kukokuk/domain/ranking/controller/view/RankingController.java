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

import java.util.List;

/**
 * 랭킹 페이지 컨트롤러
 */
@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService rankingService;

    // 기본 랭킹 상수
    private static final int DEFAULT_RANKING_LIMIT = 10;

    // 지원하는 컨텐츠 타입
    private static final String CONTENT_TYPE_SPEED = "SPEED";
    private static final String CONTENT_TYPE_DICTATION = "DICTATION";
    private static final String CONTENT_TYPE_LEVEL = "LEVEL";

    /**
     * 전체 랭킹 대시보드 페이지 - 모든 게임모드 랭킹을 가로로 배치
     * @param securityUser 로그인 사용자
     * @param model 뷰 모델
     * @return 랭킹 대시보드 페이지
     */
    @GetMapping
    public String rankingDashboardPage(
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {

        log.info("rankingDashboardPage 컨트롤러 실행");

        // 각 게임모드별 랭킹 조회 (TOP 10)
        List<Ranking> speedRankings = rankingService.getGlobalRankings(CONTENT_TYPE_SPEED, DEFAULT_RANKING_LIMIT);
        List<Ranking> dictationRankings = rankingService.getGlobalRankings(CONTENT_TYPE_DICTATION, DEFAULT_RANKING_LIMIT);
        List<Ranking> levelRankings = rankingService.getGlobalRankings(CONTENT_TYPE_LEVEL, DEFAULT_RANKING_LIMIT);

        // 로그인 사용자의 각 게임모드별 순위 정보
        Integer userSpeedRanking = null;
        Integer userDictationRanking = null;
        Integer userLevelRanking = null;
        Ranking userSpeedInfo = null;
        Ranking userDictationInfo = null;
        Ranking userLevelInfo = null;

        if (securityUser != null) {
            int userNo = securityUser.getUser().getUserNo();

            // 각 게임모드별 사용자 순위 및 정보 조회
            userSpeedRanking = rankingService.getGlobalUserRanking(userNo, CONTENT_TYPE_SPEED);
            userDictationRanking = rankingService.getGlobalUserRanking(userNo, CONTENT_TYPE_DICTATION);
            userLevelRanking = rankingService.getGlobalUserRanking(userNo, CONTENT_TYPE_LEVEL);

            userSpeedInfo = rankingService.getRankingByUserAndContent(userNo, CONTENT_TYPE_SPEED);
            userDictationInfo = rankingService.getRankingByUserAndContent(userNo, CONTENT_TYPE_DICTATION);
            userLevelInfo = rankingService.getRankingByUserAndContent(userNo, CONTENT_TYPE_LEVEL);
        }

        // 뷰에 데이터 전달
        model.addAttribute("speedRankings", speedRankings);
        model.addAttribute("dictationRankings", dictationRankings);
        model.addAttribute("levelRankings", levelRankings);

        model.addAttribute("userSpeedRanking", userSpeedRanking);
        model.addAttribute("userDictationRanking", userDictationRanking);
        model.addAttribute("userLevelRanking", userLevelRanking);

        model.addAttribute("userSpeedInfo", userSpeedInfo);
        model.addAttribute("userDictationInfo", userDictationInfo);
        model.addAttribute("userLevelInfo", userLevelInfo);

        return "ranking/dashboard";
    }
}