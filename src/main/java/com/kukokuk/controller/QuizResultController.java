package com.kukokuk.controller;

import com.kukokuk.response.QuizResultResponse;
import com.kukokuk.service.QuizResultService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/quiz/result")
public class QuizResultController {

    private final QuizResultService quizResultService;

    /**
     * 특정 세션의 퀴즈 결과를 JSON으로 반환한다.
     * @param sessionNo 세션 번호0
     * @param userNo 사용자 번호
     * @return 문제별 결과 리스트 (JSON)
     */
    @GetMapping("/{sessionNo}")
    public List<QuizResultResponse> getQuizResults(
        @PathVariable int sessionNo,
        @RequestParam int userNo) {
        log.info("getQuizResults() 실행됨 {}", sessionNo);
        log.info("getQuizResults() 실행됨 {}", userNo);
        return quizResultService.getQuizResultsBySession(sessionNo, userNo);
    }

    // QuizResultController.java

    // 📁 com.kukokuk.controller.QuizResultController.java
//    @PostMapping("/quiz-results")
//    public ResponseEntity<?> submitQuizResults(@RequestBody QuizSubmitRequest request) {
//        try {
//            int sessionNo = quizResultService.saveQuizResults(request);
//            return ResponseEntity.ok(Map.of("sessionNo", sessionNo));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("message", "퀴즈 제출 실패", "error", e.getMessage()));
//        }
//    }


}
