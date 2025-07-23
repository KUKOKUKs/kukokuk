package com.kukokuk.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Log4j2
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        log.info("home() 컨트롤러 실행");
        return "index";
    }

    @GetMapping("/login")
    public String loginform() {
        log.info("loginform() 컨트롤러 실행");
        return "user/login";
    }

}
