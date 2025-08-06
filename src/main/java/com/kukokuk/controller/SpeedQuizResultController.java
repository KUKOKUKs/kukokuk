package com.kukokuk.controller;

import com.kukokuk.request.QuizSubmitRequest;
import com.kukokuk.request.QuizSubmitResultRequest;
import com.kukokuk.response.QuizMasterResponse;
import com.kukokuk.response.QuizResultResponse;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.QuizProcessService;
import com.kukokuk.service.QuizResultService;
import com.kukokuk.service.QuizService;
import com.kukokuk.service.QuizSessionSummaryService;
import com.kukokuk.vo.QuizResult;
import com.kukokuk.vo.QuizSessionSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.ArrayList;
import java.util.List;

/**
 * 스피드 퀴즈 결과 저장 및 결과 페이지 렌더링 컨트롤러
 */
@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/quiz")
public class SpeedQuizResultController {
    private final QuizService quizService;
    private final QuizProcessService quizProcessService;
    private final QuizSessionSummaryService quizSessionSummaryService;
    private final QuizResultService quizResultService;

    /**
     * 퀴즈 결과 저장 처리 및 요약 결과 페이지 리다이렉트
     */
    @PostMapping("/result")
    public String submitQuizResults(@ModelAttribute QuizSubmitRequest request,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        int userNo = securityUser.getUser().getUserNo();
        log.info("폼 제출 - userNo: {}, totalTimeSec: {}", userNo, request.getTotalTimeSec());

        // 세션 요약 객체 초기화
        QuizSessionSummary summary = new QuizSessionSummary();
        summary.setUserNo(userNo);
        summary.setTotalTimeSec(request.getTotalTimeSec());

        List<QuizResult> quizResults = new ArrayList<>();
        for (QuizSubmitResultRequest r : request.getResults()) {
            QuizResult qr = new QuizResult();
            qr.setUserNo(userNo);
            qr.setQuizNo(r.getQuizNo());
            qr.setSelectedChoice(r.getSelectedChoice());
            quizResults.add(qr);
        }

        // 세션 저장 및 결과 저장 → sessionNo 반환받음
        int sessionNo = quizProcessService.insertQuizSessionAndResults(summary, quizResults);


        return "redirect:/quiz/result?sessionNo=" + sessionNo;
    }

    /**
     * 퀴즈 결과 요약 페이지 렌더링 (speed-result1)
     */
    @GetMapping("/result")
    public String viewSpeedQuizSummary(@RequestParam int sessionNo,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        int userNo = securityUser.getUser().getUserNo();
        log.info("결과 요약 페이지 요청 - sessionNo: {}, userNo: {}", sessionNo, userNo);

        QuizSessionSummary summary = quizSessionSummaryService.getSummaryBySessionNoAndUserNo(sessionNo, userNo);
        model.addAttribute("summary", summary);
        return "quiz/speed-result1";
    }

    /**
     * 상세 결과 페이지 렌더링 (speed-result2)
     */
    @GetMapping("/result/detail")
    public String viewSpeedResultDetail(@RequestParam int sessionNo,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        int userNo = securityUser.getUser().getUserNo();
        QuizSessionSummary summary = quizSessionSummaryService.getSummaryBySessionNoAndUserNo(sessionNo, userNo);
        List<QuizResultResponse> results = quizResultService.getQuizResultsBySession(sessionNo, userNo);

        model.addAttribute("summary", summary);
        model.addAttribute("results", results);
        return "quiz/speed-result2";
    }

    /**
     * 풀이횟수가 20회 이하인 스피드 퀴즈 10개를 조회하여 반환한다.
     *
     * @return QuizMasterResponse 리스트
     */
    @GetMapping("/speed")
    public String viewSpeedQuizList(Model model) {
        int usageThreshold = 20;
        int limit = 10;
        List<QuizMasterResponse> quizList = quizService.getSpeedQuizList(usageThreshold, limit)
            .stream()
            .map(QuizMasterResponse::from)
            .toList();

        model.addAttribute("quizList", quizList);
        return "quiz/speed"; // templates/quiz/speed.html
    }



}
