package com.kukokuk.controller;

import com.kukokuk.dto.MainStudyViewDto;
import com.kukokuk.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class StudyController {

  private final StudyService studyService;


  @GetMapping("/study")
  public String studyMain(Model model,
      @AuthenticationPrincipal UserDetails userDetails){
    MainStudyViewDto dto = studyService.getMainStudyView(userDetails);
    model.addAttribute("data", dto);
    System.out.println(dto.getUser().getUserNo());
    return "study/main";
  }
}
