package com.kukokuk.domain.study.controller.view;

import com.kukokuk.domain.study.dto.MainStudyViewDto;
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

    @GetMapping("/test")
    public String testHtml() {
        return "study/test";
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

}
