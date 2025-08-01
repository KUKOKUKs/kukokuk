package com.kukokuk.controller;

import com.kukokuk.request.QuizSubmitRequest;
import com.kukokuk.request.QuizSubmitResultRequest;
import com.kukokuk.response.QuizResultResponse;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.QuizProcessService;
import com.kukokuk.service.QuizResultService;
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

    private final QuizProcessService quizProcessService;
    private final QuizSessionSummaryService quizSessionSummaryService;
    private final QuizResultService quizResultService;

    /**
     * 스피드 퀴즈 풀이 결과를 저장한다 (폼 기반 제출)
     * @param request 사용자가 제출한 퀴즈 결과
     * @param securityUser 로그인 사용자 정보
     * @param model 결과 페이지에 전달할 모델
     * @return speed-result.html 뷰로 이동
     */
    @PostMapping("/result")
    public String submitQuizResults(@ModelAttribute QuizSubmitRequest request,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        int userNo = securityUser.getUser().getUserNo();
        log.info("폼 제출 - userNo: {}, totalTimeSec: {}", userNo, request.getTotalTimeSec());

        QuizSessionSummary summary = new QuizSessionSummary();
        summary.setUserNo(userNo);
        summary.setTotalTimeSec(request.getTotalTimeSec());
        summary.setTotalQuestion(request.getResults().size());

        List<QuizResult> quizResults = new ArrayList<>();
        for (QuizSubmitResultRequest r : request.getResults()) {
            QuizResult qr = new QuizResult();
            qr.setUserNo(userNo);
            qr.setQuizNo(r.getQuizNo());
            qr.setSelectedChoice(r.getSelectedChoice());
            quizResults.add(qr);
        }

        quizProcessService.insertQuizSessionAndResults(summary, quizResults);

        // 결과 페이지로 redirect (세션 번호 전달)
        return "redirect:/quiz/speed-result?sessionNo=" + summary.getSessionNo();
    }

    /**
     * 결과 페이지 렌더링
     * @param sessionNo 세션 번호
     * @param securityUser 로그인 유저
     * @param model 모델 전달
     * @return speed-result.html 뷰
     */
    @GetMapping("/speed-result")
    public String viewSpeedResult(@RequestParam int sessionNo,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        log.info("securityUser = {}", securityUser);
        int userNo = securityUser.getUser().getUserNo();
        log.info("스피드 결과 요청 - sessionNo: {}, userNo: {}", sessionNo, userNo);

        QuizSessionSummary summary = quizSessionSummaryService.getSummaryBySessionNoAndUserNo(sessionNo, userNo);
        List<QuizResultResponse> results = quizResultService.getQuizResultsBySession(sessionNo, userNo);

        model.addAttribute("summary", summary);
        model.addAttribute("results", results);
        return "quiz/speed-result";
    }
}
