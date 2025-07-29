package com.kukokuk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// 📁 com.kukokuk.controller.QuizViewController.java
@Controller
public class QuizViewController {

    /**
     * 스피드 퀴즈 화면
     */
    @GetMapping("/quiz/speed")
    public String showSpeedQuizPage() {
        return "quiz/speed"; //templates/quiz/speed.html 렌더링
    }
}
