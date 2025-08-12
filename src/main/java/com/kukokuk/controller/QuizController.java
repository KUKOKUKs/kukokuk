package com.kukokuk.controller;

import com.kukokuk.dto.QuizLevelResultDto;
import com.kukokuk.dto.QuizMasterDto;
import com.kukokuk.dto.QuizResultDto;
import com.kukokuk.dto.QuizSubmitDto;
import com.kukokuk.dto.QuizSubmitResultDto;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.QuizBookmarkService;
import com.kukokuk.service.QuizProcessService;
import com.kukokuk.service.QuizResultService;
import com.kukokuk.service.QuizService;
import com.kukokuk.service.QuizSessionSummaryService;
import com.kukokuk.vo.QuizMaster;
import com.kukokuk.vo.QuizResult;
import com.kukokuk.vo.QuizSessionSummary;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;
    private final QuizProcessService quizProcessService;
    private final QuizSessionSummaryService quizSessionSummaryService;
    private final QuizResultService quizResultService;
    private final QuizBookmarkService quizBookmarkService; // ★추가

    //[퀴즈 선택페이지]뷰이동
    @GetMapping("/main")
    public String viewMain(){
        log.info("[확인] viewMain() 페이지 컨트롤러 실행");
        return "quiz/main";
    }

    // [단계별 퀴즈] 난이도 선택 후 문제 10개 조회
    @GetMapping("/level")
    public String viewLevelQuizList(@RequestParam(required = false) String difficulty,
        @RequestParam(required = false) String questionType,
        Model model) {
        if (difficulty == null || questionType == null) {
            return "redirect:/quiz/level-select"; // 값 없으면 다시 선택 화면으로
        }

        List<QuizMaster> quizList = quizService.getLevelQuizList(difficulty, questionType);

        model.addAttribute("quizList", quizList);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("questionType", questionType);
        return "quiz/level";
    }


    // [공통] 퀴즈 결과 저장 처리
    @PostMapping("/result")
    public String submitQuizResults(@ModelAttribute QuizSubmitDto request,
        @AuthenticationPrincipal SecurityUser securityUser) {
        int userNo = securityUser.getUser().getUserNo();

        QuizSessionSummary summary = new QuizSessionSummary();
        summary.setUserNo(userNo);
        summary.setTotalTimeSec(request.getTotalTimeSec());
        summary.setQuizMode("level".equals(request.getQuizMode()) ? "level" : "speed");

        log.info("[컨트롤러] summary.quizMode={}", summary.getQuizMode());

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

    // [공통] 퀴즈 결과 요약 페이지 렌더링
    @GetMapping("/result")
    public String viewQuizSummary(@RequestParam int sessionNo,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        int userNo = securityUser.getUser().getUserNo();
        QuizSessionSummary summary = quizSessionSummaryService.getSummaryBySessionNoAndUserNo(sessionNo, userNo);
        model.addAttribute("summary", summary);

        // DTO로 조회 및 세팅
        QuizLevelResultDto levelResult = quizService.getDifficultyAndQuestionTypeBySessionNo(sessionNo);
        model.addAttribute("difficulty", levelResult.getDifficulty());
        model.addAttribute("questionType", levelResult.getQuestionType());

        if ("level".equals(summary.getQuizMode())) {
            return "quiz/level-result1";
        }
        return "quiz/speed-result1";
    }

    // [공통] 퀴즈 결과 상세 페이지 렌더링
    @GetMapping("/result/detail")
    public String viewQuizResultDetail(@RequestParam int sessionNo,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        int userNo = securityUser.getUser().getUserNo();
        QuizSessionSummary summary = quizSessionSummaryService.getSummaryBySessionNoAndUserNo(sessionNo, userNo);
        List<QuizResultDto> results = quizResultService.getQuizResultsBySession(sessionNo, userNo);

        model.addAttribute("summary", summary);
        model.addAttribute("results", results);
        QuizLevelResultDto levelResult = quizService.getDifficultyAndQuestionTypeBySessionNo(sessionNo);
        model.addAttribute("difficulty", levelResult.getDifficulty());
        model.addAttribute("questionType", levelResult.getQuestionType());

        if ("level".equals(summary.getQuizMode())) {
            return "quiz/level-result2";
        }
        return "quiz/speed-result2";
    }

    // [스피드 퀴즈] 문제 10개 조회
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

    @GetMapping("/select")
    public String selectQuizMode() {
        return "quiz/select";
    }

    @GetMapping("/level-select")
    public String selectLevelQuizSetting() {
        return "quiz/level-select";
    }


    /**
     * [북마크] 내가 북마크한 퀴즈 목록
     * @param securityUser 로그인한 사용자
     * @param model 뷰 모델
     * @return 북마크 목록 뷰 이름
     */
    @GetMapping("/bookmark-list")
    public String bookmarkListPage(
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        int userNo = securityUser.getUser().getUserNo();
        List<QuizMaster> quizList = quizBookmarkService.getBookmarkedQuizList(userNo);
        model.addAttribute("quizList", quizList);
        model.addAttribute("listType", "bookmark");
        return "quiz/bookmark"; // templates/quiz/bookmark.html

    }




}
