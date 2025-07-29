package com.kukokuk.controller;

import com.kukokuk.request.QuizSubmitRequest;
import com.kukokuk.request.QuizSubmitResultRequest;
import com.kukokuk.response.QuizResultResponse;
import com.kukokuk.service.QuizProcessService;
import com.kukokuk.service.QuizResultService;
import com.kukokuk.vo.QuizResult;
import com.kukokuk.vo.QuizSessionSummary;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/quiz/result")
public class QuizResultController {

    private final QuizResultService quizResultService;
    private final QuizProcessService quizProcessService;

    /**
     * 퀴즈 정답 제출 및 세션 저장
     * @param request 사용자 제출 데이터
     * @return 생성된 세션 번호
     */
    @PostMapping
    public ResponseEntity<Integer> submitQuizResults(@RequestBody QuizSubmitRequest request) {
        // 1. 세션 요약 생성
        QuizSessionSummary summary = new QuizSessionSummary();
        summary.setUserNo(request.getUserNo());
        summary.setTotalTimeSec(request.getTotalTimeSec());

        int totalQuestions = request.getResults().size();
        summary.setTotalQuestion(totalQuestions);

        int correctAnswers = 0;
        for (QuizSubmitResultRequest r : request.getResults()) {
            if ("Y".equals(r.getIsSuccess())) correctAnswers++;
        }
        summary.setCorrectAnswers(correctAnswers);
        summary.setAverageTimePerQuestion((float) request.getTotalTimeSec() / totalQuestions);

        // 2. 퀴즈 결과 리스트 생성
        List<QuizResult> quizResults = new ArrayList<>();
        for (QuizSubmitResultRequest r : request.getResults()) {
            QuizResult qr = new QuizResult();
            qr.setUserNo(request.getUserNo());
            qr.setQuizNo(r.getQuizNo());
            qr.setSelectedChoice(r.getSelectedChoice());
            qr.setIsSuccess(r.getIsSuccess());
            qr.setIsBookmarked(r.getIsBookmarked());
            quizResults.add(qr);
        }

        // 3. 서비스 호출
        quizProcessService.insertQuizSessionAndResults(summary, quizResults);

        // 4. 생성된 세션 번호 응답
        return ResponseEntity.ok(summary.getSessionNo());
    }

    /**
     * 특정 세션의 퀴즈 결과를 조회한다.
     * @param sessionNo 퀴즈 세션 번호
     * @param userNo 사용자 번호
     * @return 퀴즈 결과 응답 리스트
     */
    @GetMapping("/{sessionNo}")
    public List<QuizResultResponse> getQuizResults(
        @PathVariable int sessionNo,
        @RequestParam int userNo) {
        log.info("getQuizResults() 실행됨 sessionNo={}, userNo={}", sessionNo, userNo);
        return quizResultService.getQuizResultsBySession(sessionNo, userNo);
    }
}
