package com.kukokuk.rest;

import com.kukokuk.request.QuizSubmitRequest;
import com.kukokuk.request.QuizSubmitResultRequest;
import com.kukokuk.response.QuizResultResponse;
import com.kukokuk.service.QuizProcessService;
import com.kukokuk.service.QuizResultService;
import com.kukokuk.vo.QuizResult;
import com.kukokuk.vo.QuizSessionSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 결과 처리용 API 컨트롤러
 */
@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz/result")
public class ApiQuizResultController {

    private final QuizResultService quizResultService;
    private final QuizProcessService quizProcessService;

    /**
     * 퀴즈 정답 제출 및 세션 저장
     * @param request 사용자 제출 데이터
     * @return 생성된 세션 번호
     */
    @PostMapping
    public ResponseEntity<Integer> submitQuizResults(@RequestBody QuizSubmitRequest request) {
        log.info("submitQuizResults() 호출됨: userNo={}, totalTimeSec={}", request.getUserNo(), request.getTotalTimeSec());

        // 1. 세션 요약 생성
        QuizSessionSummary summary = new QuizSessionSummary();
        summary.setUserNo(request.getUserNo());
        summary.setTotalTimeSec(request.getTotalTimeSec());

        int totalQuestions = request.getResults().size();
        summary.setTotalQuestion(totalQuestions);

        // 정답 수는 QuizProcessService에서 판단됨
        List<QuizResult> quizResults = new ArrayList<>();
        for (QuizSubmitResultRequest r : request.getResults()) {
            QuizResult qr = new QuizResult();
            qr.setUserNo(request.getUserNo());
            qr.setQuizNo(r.getQuizNo());
            qr.setSelectedChoice(r.getSelectedChoice());
            qr.setIsBookmarked(r.getIsBookmarked());
            quizResults.add(qr);
        }

        // 2. 저장 처리 + 내부에서 정답 판단 포함
        quizProcessService.insertQuizSessionAndResults(summary, quizResults);

        // 3. 생성된 세션 번호 반환
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
