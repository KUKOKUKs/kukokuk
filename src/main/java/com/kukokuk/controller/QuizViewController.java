package com.kukokuk.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 퀴즈 뷰 템플릿 전용 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class QuizViewController {

    /**
     * 스피드 퀴즈 페이지
     */
    @GetMapping("/quiz/speed")
    public String showSpeedQuizPage() {
        return "quiz/speed"; // templates/quiz/speed.html
    }



}
