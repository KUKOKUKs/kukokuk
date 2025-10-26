package com.kukokuk.domain.quiz.controller.view;

import com.kukokuk.common.constant.ContentTypeEnum;
import com.kukokuk.common.constant.PaginationEnum;
import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.quiz.dto.QuizLevelResultDto;
import com.kukokuk.domain.quiz.dto.QuizResultDto;
import com.kukokuk.domain.quiz.dto.QuizSubmitDto;
import com.kukokuk.domain.quiz.dto.QuizSubmitResultDto;
import com.kukokuk.domain.quiz.service.QuizBookmarkService;
import com.kukokuk.domain.quiz.service.QuizProcessService;
import com.kukokuk.domain.quiz.service.QuizResultService;
import com.kukokuk.domain.quiz.service.QuizService;
import com.kukokuk.domain.quiz.service.QuizSessionSummaryService;
import com.kukokuk.domain.quiz.vo.QuizMaster;
import com.kukokuk.domain.quiz.vo.QuizResult;
import com.kukokuk.domain.quiz.vo.QuizSessionSummary;
import com.kukokuk.domain.user.service.UserService;
import com.kukokuk.domain.user.vo.User;
import com.kukokuk.security.SecurityUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Log4j2

@Controller
@RequiredArgsConstructor
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;
    private final QuizProcessService quizProcessService;
    private final QuizSessionSummaryService quizSessionSummaryService;
    private final QuizResultService quizResultService;
    private final QuizBookmarkService quizBookmarkService;
    private final UserService userService;

    /**
     * 퀴즈 메인 페이지 (학습이력 위젯 포함) 퀴즈 선택 + 최근 학습이력을 함께 표시
     */
    @GetMapping
    public String viewMain(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        log.info("[퀴즈 메인] viewMain() 페이지 컨트롤러 실행");

        int userNo = securityUser.getUser().getUserNo();
        log.info("[퀴즈 메인] 사용자 {}의 학습이력 조회", userNo);

        // 각 도메인에서 최근 이력 조회 (5개씩만 - 메인 페이지용)
        List<QuizSessionSummary> speedHistory = quizSessionSummaryService.getQuizSessionSummaryByUserNoAndMode(
            userNo
            , ContentTypeEnum.SPEED.name().toLowerCase(Locale.ROOT)
            , PaginationEnum.COMPONENT_ROWS);
        List<QuizSessionSummary> levelHistory = quizSessionSummaryService.getQuizSessionSummaryByUserNoAndMode(
            userNo
            , ContentTypeEnum.LEVEL.name().toLowerCase(Locale.ROOT)
            , PaginationEnum.COMPONENT_ROWS);

//         List<DictationSession> dictationHistory = dictationService.getRecentDictationHistory(userNo, 3);

        // Model에 학습이력 데이터 추가
        model.addAttribute("speedHistory", speedHistory);
        model.addAttribute("levelHistory", levelHistory);
        // model.addAttribute("dictationHistory", dictationHistory);

        return "quiz/main";
    }

    // 단계별 퀴즈 선택 페이지
    @GetMapping("/level-select")
    public String selectLevelQuizSetting() {
        log.info("QuizController selectLevelQuizSetting() 실행");
        return "quiz/level-select";
    }

    // [단계별 퀴즈] 난이도 선택 후 문제 10개 조회
    @GetMapping("/level")
    public String viewLevelQuizList(
        @RequestParam String difficulty
        , @RequestParam String questionType
        , Model model) {
        log.info("QuizController viewLevelQuizList() 실행");

        model.addAttribute("quizList", quizService.getLevelQuizList(difficulty, questionType));
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

        // 요약 정보 조회
        QuizSessionSummary summary = quizSessionSummaryService.getSummaryBySessionNoAndUserNo(
            sessionNo, userNo);

        // 상세 결과 데이터도 함께 조회하여 전달
        List<QuizResultDto> results = quizResultService.getQuizResultsBySession(sessionNo, userNo);

        // 모델에 데이터 추가
        model.addAttribute("summary", summary);
        model.addAttribute("results", results);

        QuizLevelResultDto levelResult = quizService.getDifficultyAndQuestionTypeBySessionNo(
            sessionNo);
        model.addAttribute("difficulty", levelResult.getDifficulty());
        model.addAttribute("questionType", levelResult.getQuestionType());

        // 퀴즈 모드에 따라 다른 뷰 반환
        if ("level".equals(summary.getQuizMode())) {
            return "quiz/level-result";
        }
        return "quiz/speed-result";
    }

    // [스피드 퀴즈] 문제 10개 조회
    @GetMapping("/speed")
    public String viewSpeedQuizList(Model model) {
        int usageThreshold = 20;
        int limit = 10;

        model.addAttribute("quizList", quizService.getSpeedQuizList(usageThreshold, limit));
        return "quiz/speed";
    }

    /**
     * [북마크] 내가 북마크한 퀴즈 목록
     *
     * @param securityUser 로그인한 사용자
     * @param model        뷰 모델
     * @return 북마크 목록 뷰 이름
     */
    @GetMapping("/bookmark")
    public String bookmarkListPage(
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        int userNo = securityUser.getUser().getUserNo();
        List<QuizMaster> quizList = quizBookmarkService.getBookmarkedQuizList(userNo);
        model.addAttribute("quizList", quizList);
        model.addAttribute("listType", "bookmark");
        /*DailyQuestEnum.QUIZ_SPEED.getDailyQuestNo()*/
        return "quiz/bookmark"; // templates/quiz/bookmark.html

    }

// QuizController 클래스 (src/main/java/com/kukokuk/domain/quiz/controller/view/QuizController.java)
// 클래스 상단에 UserService 의존성 주입 추가 필요:
// private final UserService userService;

    /**
     * 퀴즈 힌트 사용 처리
     *
     * @param quizIndex     퀴즈 인덱스
     * @param removedOption 제거된 보기 번호
     * @param securityUser  로그인 사용자 정보
     * @return 남은 힌트 개수
     */
    @PostMapping("/use-hint")
    @ResponseBody
    public ResponseEntity<ApiResponse<Integer>> useHint(
        @RequestParam("quizIndex") int quizIndex,
        @RequestParam("removedOption") int removedOption,
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        log.info("퀴즈 힌트 사용 처리 - quizIndex: {}, removedOption: {}", quizIndex, removedOption);

        try {
            int userNo = securityUser.getUser().getUserNo();

            // 사용자 힌트 개수 차감
            userService.updateUserHintCountMinus(userNo);

            // 차감 후 남은 힌트 개수 조회
            User updatedUser = userService.getUserByUserNo(userNo);
            int remainingHints = updatedUser.getHintCount();

            log.info("퀴즈 힌트 사용 완료 - 남은 힌트: {}", remainingHints);

            return ResponseEntityUtils.ok(remainingHints);

        } catch (Exception e) {
            log.error("퀴즈 힌트 사용 처리 실패", e);
            ApiResponse<Integer> errorResponse = new ApiResponse<>(false, 500, "힌트 사용에 실패했습니다.",
                null);
            return ResponseEntity.status(500).body(errorResponse);
        }


    }

}
