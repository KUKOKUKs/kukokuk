package com.kukokuk.controller;

import com.kukokuk.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StudyController {

  private final StudyService studyService;


  @GetMapping("/study")
  public String studyMain(Model model,
      @AuthenticationPrincipal UserDetails userDetails){
    studyService.getMainStudyView(userDetails);
    return "study/main";
  }
}
