package com.kukokuk.controller;

import com.kukokuk.dto.MainStudyViewDto;
import com.kukokuk.dto.StudyProgressViewDto;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @GetMapping("/study")
    public String studyMain(Model model,
        @AuthenticationPrincipal SecurityUser securityUser) {
        MainStudyViewDto dto = studyService.getMainStudyView(securityUser);
        model.addAttribute("data", dto);
        return "study/main";
    }

    @GetMapping("/study/{dailyStudyNo}")
    public String studyProgress(@PathVariable("dailyStudyNo") int dailyStudyNo,
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser) {

        StudyProgressViewDto dto = studyService.getStudyProgressView(dailyStudyNo,
            securityUser.getUser().getUserNo());
        model.addAttribute("data", dto);

        return "study/progress";
    }
}
