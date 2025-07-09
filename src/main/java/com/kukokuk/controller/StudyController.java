package com.kukokuk.controller;

import com.kukokuk.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudyController {

  @Autowired
  private StudyService studyService;

  @GetMapping("/study")
  public String studyMain(Model model){
    studyService.getMainStudyView();
    return "study/main";
  }
}
