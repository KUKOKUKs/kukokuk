package com.kukokuk.domain.study.controller.view;

import com.kukokuk.domain.study.service.StudyService;
import com.kukokuk.domain.study.vo.MaterialParseJob;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/studies")
@RequiredArgsConstructor
public class AdminController {

    private final StudyService studyService;

    @GetMapping("/parse-materials")
    public String parseMaterialView(Model model) {
        List<MaterialParseJob> jobs = studyService.getMaterialParseJobs();
        model.addAttribute("parseJobs", jobs);

        return "admin/parse-materials";
    }

}
