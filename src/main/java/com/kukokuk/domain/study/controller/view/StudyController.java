package com.kukokuk.domain.study.controller.view;

import com.kukokuk.domain.study.dto.DailyStudyLogDetailResponse;
import com.kukokuk.domain.study.dto.MainStudyViewDto;
import com.kukokuk.domain.study.dto.StudyCompleteViewDto;
import com.kukokuk.domain.study.dto.StudyEssayViewDto;
import com.kukokuk.domain.study.dto.StudyProgressViewDto;
import com.kukokuk.domain.study.service.StudyService;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/study")
public class StudyController {

    private final StudyService studyService;

    @GetMapping
    public String studyMain(Model model,
        @AuthenticationPrincipal SecurityUser securityUser) {
        MainStudyViewDto dto = studyService.getMainStudyView(securityUser);
        model.addAttribute("data", dto);
        return "study/main";
    }

    @GetMapping("/{dailyStudyNo}")
    public String studyProgress(@PathVariable("dailyStudyNo") int dailyStudyNo,
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser) {

        StudyProgressViewDto dto = studyService.getStudyProgressView(dailyStudyNo,
            securityUser.getUser().getUserNo());
        model.addAttribute("data", dto);

        return "study/progress";
    }

    @GetMapping("/{dailyStudyNo}/complete")
    public String studyComplete(
        @PathVariable("dailyStudyNo") int dailyStudyNo,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {

        StudyCompleteViewDto dto = studyService.getStudyCompleteView(dailyStudyNo, securityUser.getUser().getUserNo());

        model.addAttribute("data", dto);
        model.addAttribute("dailyStudyNo", dailyStudyNo);

        return "study/complete";
    }

    @GetMapping("/{dailyStudyNo}/essay")
    public String studyEssay(@PathVariable("dailyStudyNo") int dailyStudyNo,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        log.info("studyEssay 컨트롤러 실행");

        Integer userNo = securityUser != null ? securityUser.getUser().getUserNo() : null;
        StudyEssayViewDto dto = studyService.getStudyEssayView(dailyStudyNo, userNo);

        model.addAttribute("data", dto);

        return "study/essay";
    }

    @GetMapping("/history")
    public String studyHistory(
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        log.info("studyHistory 컨트롤러 실행");

        // (1) 초기 렌더링용 기본 페이지/행 수 설정
        int page = 1;
        int rows = 10;

        // (2) 서비스 호출: 학습 이력 상세 목록 조회 (최신순)
        List<DailyStudyLogDetailResponse> studyLogs =
            studyService.getStudyLogsDetail(securityUser.getUser().getUserNo(), page, rows);

        int totalStudyLogCount = studyService.getStudyLogCount(securityUser.getUser().getUserNo());

        // (3) 모델에 데이터 담기
        model.addAttribute("studyLogs", studyLogs);
        model.addAttribute("totalStudyLogCount", totalStudyLogCount);
        model.addAttribute("rows", rows);
        model.addAttribute("page", page);

        return "study/history";
    }

}
