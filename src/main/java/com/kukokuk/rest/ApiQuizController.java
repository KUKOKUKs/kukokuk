package com.kukokuk.rest;

import com.kukokuk.response.QuizMasterResponse;
import com.kukokuk.service.QuizService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class ApiQuizController {

    private final QuizService quizService;

    /**
     * 풀이횟수가 20회 이하인 스피드 퀴즈 10개를 조회하여 반환한다.
     *
     * @return QuizMasterResponse 리스트 (JSON 배열 형태)
     */

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