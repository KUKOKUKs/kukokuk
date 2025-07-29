package com.kukokuk.controller;

import com.kukokuk.response.QuizMasterResponse;
import com.kukokuk.service.QuizService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    /**
     * 랜덤 퀴즈를 N개 생성 요청
     * @param count 생성할 퀴즈 수
     */
    @GetMapping("/generate-random")
    public ResponseEntity<String> generateRandomQuizSet(@RequestParam(defaultValue = "100") int count) {

        quizService.insertRandomQuizBulk(count);
        return ResponseEntity.ok(count + "개의 랜덤 퀴즈가 생성되었습니다.");
    }


    @GetMapping("/speed")
    public List<QuizMasterResponse> getSpeedQuizList() {
        int usageThreshold = 20;
        int limit = 10;
        return quizService.getSpeedQuizList(usageThreshold, limit)
            .stream()
            .map(QuizMasterResponse::from)
            .toList();
    }

}