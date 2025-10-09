package com.kukokuk.common.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
public class CommonController {

    // 메인 페이지
    @GetMapping("/")
    public String home() {
        log.info("home() 컨트롤러 실행");
        return "index";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginform() {
        log.info("loginform() 컨트롤러 실행");
        return "user/login";
    }

    // 권한 부족 페이지
    @GetMapping("/access-denied")
    public String accessDeniedPage(HttpServletResponse response, Model model) {
        log.error("accessDeniedPage() 컨트롤러 실행");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute("status", 403);
        model.addAttribute("error", "Forbidden");
        model.addAttribute("message", "접근 권한이 없습니다.");
        log.error("[{}] {} - {}", HttpServletResponse.SC_FORBIDDEN, "Forbidden", "접근 권한이 없습니다.");
        return "/resources/templates/error/error-page.html";
    }

}
