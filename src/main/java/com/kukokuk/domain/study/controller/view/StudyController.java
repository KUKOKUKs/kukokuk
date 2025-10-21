package com.kukokuk.domain.study.controller.view;

import com.kukokuk.common.constant.PaginationEnum;
import com.kukokuk.domain.study.dto.StudyCompleteViewDto;
import com.kukokuk.domain.study.dto.StudyEssayViewDto;
import com.kukokuk.domain.study.dto.StudyProgressViewDto;
import com.kukokuk.domain.study.service.StudyService;
import com.kukokuk.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/study")
public class StudyController {

    private final StudyService studyService;

    // 학습 페이지
    @GetMapping
    public String studyMain(Model model,
        @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("StudyController studyMain() 실행");
        
        // 학습 이력 정보
        model.addAttribute(
            "dailyStudyLogs"
            , studyService.getDailyStudyLogs(securityUser.getUser().getUserNo(), PaginationEnum.COMPONENT_ROWS)
        );
        return "study/main";
    }

    // 학습 진행 시작 페이지(학습 이력 상세 페이지 겸용?)
    @GetMapping("/{dailyStudyNo}")
    public String studyProgress(@PathVariable("dailyStudyNo") int dailyStudyNo,
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser) {

        StudyProgressViewDto studyProgressDto = studyService.getStudyProgressView(dailyStudyNo,
            securityUser.getUser().getUserNo());
        model.addAttribute("studyProgressDto", studyProgressDto);

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

    // 학습 이력 전용 페이지
    @GetMapping("/history")
    public String studyHistory(
        @RequestParam(defaultValue = "1") int page
        , @RequestParam(required = false) Integer rows
        , @AuthenticationPrincipal SecurityUser securityUser
        , Model model) {
        log.info("StudyController studyHistory() 컨트롤러 실행");

        // 조회할 행의 수를 입력하지 않았을 경우 기본 값 10
        if (rows == null) {
            rows = PaginationEnum.DEFAULT_ROWS;
        }

        model.addAttribute("studyLogs", studyService.getStudyLogsDetail(securityUser.getUser().getUserNo(), page, rows));

        return "study/history";
    }

}
