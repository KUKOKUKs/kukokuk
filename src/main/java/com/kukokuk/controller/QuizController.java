package com.kukokuk.controller;

import com.kukokuk.dto.QuizMasterDto;
import com.kukokuk.dto.QuizResultDto;
import com.kukokuk.dto.QuizSubmitDto;
import com.kukokuk.dto.QuizSubmitResultDto;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.QuizProcessService;
import com.kukokuk.service.QuizResultService;
import com.kukokuk.service.QuizService;
import com.kukokuk.service.QuizSessionSummaryService;
import com.kukokuk.vo.QuizMaster;
import com.kukokuk.vo.QuizResult;
import com.kukokuk.vo.QuizSessionSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;
    private final QuizProcessService quizProcessService;
    private final QuizSessionSummaryService quizSessionSummaryService;
    private final QuizResultService quizResultService;

    /**
     * [공통] 퀴즈 결과 저장 처리
     */
    @PostMapping("/result")
    public String submitQuizResults(@ModelAttribute QuizSubmitDto request,
        @AuthenticationPrincipal SecurityUser securityUser) {
        int userNo = securityUser.getUser().getUserNo();

        QuizSessionSummary summary = new QuizSessionSummary();
        summary.setUserNo(userNo);
        summary.setTotalTimeSec(request.getTotalTimeSec());

        // 사용자가 어떤 모드에서 푼 것인지 명시
        if ("level".equals(request.getQuizMode())) {
            summary.setQuizMode("level");
        } else {
            summary.setQuizMode("speed");
        }

        List<QuizResult> results = new ArrayList<>();
        for (QuizSubmitResultDto r : request.getResults()) {
            QuizResult qr = new QuizResult();
            qr.setUserNo(userNo);
            qr.setQuizNo(r.getQuizNo());
            qr.setSelectedChoice(r.getSelectedChoice());
            results.add(qr);
        }

        int sessionNo = quizProcessService.insertQuizSessionAndResults(summary, results);
        return "redirect:/quiz/result?sessionNo=" + sessionNo;
    }

    /**
     * [공통] 퀴즈 결과 요약 페이지 렌더링
     */
    @GetMapping("/result")
    public String viewQuizSummary(@RequestParam int sessionNo,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        int userNo = securityUser.getUser().getUserNo();
        QuizSessionSummary summary = quizSessionSummaryService.getSummaryBySessionNoAndUserNo(
            sessionNo, userNo);
        model.addAttribute("summary", summary);

        if ("level".equals(summary.getQuizMode())) {
            return "quiz/level-result1";
        }
        return "quiz/speed-result1";
    }

    /**
     * [공통] 퀴즈 결과 상세 페이지 렌더링
     */
    @GetMapping("/result/detail")
    public String viewQuizResultDetail(@RequestParam int sessionNo,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        int userNo = securityUser.getUser().getUserNo();
        QuizSessionSummary summary = quizSessionSummaryService.getSummaryBySessionNoAndUserNo(
            sessionNo, userNo);
        List<QuizResultDto> results = quizResultService.getQuizResultsBySession(sessionNo, userNo);

        model.addAttribute("summary", summary);
        model.addAttribute("results", results);

        if ("level".equals(summary.getQuizMode())) {
            return "quiz/level-result2";
        }
        return "quiz/speed-result2";
    }

    /**
     * [스피드 퀴즈] 문제 10개 조회
     */
    @GetMapping("/speed")
    public String viewSpeedQuizList(Model model) {
        int usageThreshold = 20;
        int limit = 10;

        List<QuizMasterDto> quizList = quizService.getSpeedQuizList(usageThreshold, limit)
            .stream()
            .map(QuizMasterDto::from)
            .toList();

        model.addAttribute("quizList", quizList);
        return "quiz/speed";
    }

    /**
     * [단계별 퀴즈] 난이도 선택 후 문제 10개 조회
     */
    @GetMapping("/level")
    public String viewLevelQuizList(@RequestParam String difficulty,
        @RequestParam String questionType,
        Model model) {
        List<QuizMaster> quizList = quizService.getLevelQuizList(difficulty, questionType);

        model.addAttribute("quizList", quizList);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("questionType", questionType);
        return "quiz/level";
    }
}
