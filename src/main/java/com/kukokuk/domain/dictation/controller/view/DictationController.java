package com.kukokuk.domain.dictation.controller.view;


import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.dictation.dto.DictationQuestionLogDto;
import com.kukokuk.domain.dictation.service.DictationService;
import com.kukokuk.domain.dictation.vo.DictationQuestion;
import com.kukokuk.domain.dictation.vo.DictationSession;
import com.kukokuk.security.SecurityUser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Log4j2
@Controller
@SessionAttributes({"dictationQuestions", "dictationQuestionLogDto", "startDate", "questionIndex"})
@RequiredArgsConstructor
@RequestMapping("/dictation")
public class DictationController {

    private final DictationService dictationService;

    @ModelAttribute("dictationQuestions")
    public List<DictationQuestion> initQuestions() {
        return new ArrayList<>();
    }

    @ModelAttribute("dictationQuestionLogDto")
    public List<DictationQuestionLogDto> initQuestionLogs() {
        return new ArrayList<>();
    }

    @ModelAttribute("startDate")
    public Date initStartDate() {
        return new Date();
    }

    @ModelAttribute("questionIndex")
    public int initQuestionIndex() {
        return 0;
    }

    /**
     * 받아쓰기 시작 요청을 처리하는 메서드 현재 로그인한 사용자의 번호를 바탕으로, 아직 풀지 않은 받아쓰기 문제 10개를 랜덤으로 가져와 세션에 저장하고, 첫 번째
     * 문제부터 풀 수 있도록 문제 풀이 페이지로 이동
     * @param securityUser 현재 로그인한 사용자 정보
     * @param dictationQuestions 세션에 저장된 받아쓰기 문제 목록
     * @param model 인덱스, 시작시각, 빈 로그 dto
     * @return 문제 풀이 페이지
     */
    @GetMapping("/start")
    public String startDictation(@AuthenticationPrincipal SecurityUser securityUser,
        @ModelAttribute("dictationQuestions") List<DictationQuestion> dictationQuestions,
        Model model) {
        log.info("@GetMapping(/start) startDictation() 실행");

        // 1) 현재 사용자 번호
        int userNo = securityUser.getUser().getUserNo();
        log.info("startDictation() 사용자 번호: {}", userNo);

        // 2) 새 문제 10개로 교체
        dictationQuestions.clear();
        dictationQuestions.addAll(dictationService.getDictationQuestionsByUserNo(userNo, 10));
        log.info("문제 로드 완료: {}개", dictationQuestions.size());

        // 3) 인덱스/시작시각/로그 초기 세팅
        model.addAttribute("questionIndex", 0);
        model.addAttribute("startDate", new Date());

        // 4) 문제 수만큼 빈 로그 DTO 리스트 세션 생성
        List<DictationQuestionLogDto> dictationQuestionLogDtoList = new ArrayList<>();
        for (int i = 0; i < dictationQuestions.size(); i++) {
            dictationQuestionLogDtoList.add(new DictationQuestionLogDto());
        }

        // 5) 세션(@SessionAttributes) 갱신
        model.addAttribute("dictationQuestionLogDto", dictationQuestionLogDtoList);
        log.info("[/start] 로그 DTO 초기화 완료: {}개", dictationQuestionLogDtoList.size());

        return "redirect:/dictation/solve";
    }

    /**
     * 받아쓰기 문제 풀이
     * @param dictationQuestions 세션에 저장된 받아쓰기 문제 목록
     * @param dictationQuestionLogDtoList 세션에 저장된 이력 dto 목록
     * @param questionIndex 세션에 저장된 현재 인덱스
     * @param model 현제 페이지
     * @return solve.html 페이지
     */
    @GetMapping("/solve")
    public String showQuestion(@ModelAttribute("dictationQuestions") List<DictationQuestion> dictationQuestions,
        @ModelAttribute("dictationQuestionLogDto") List<DictationQuestionLogDto> dictationQuestionLogDtoList,
        @ModelAttribute("questionIndex") int questionIndex,
        Model model) {
        log.info("[/solve] questionIndex: {} / size: {}", questionIndex, dictationQuestions.size());


        // 마지막 문제 처리 완료 시 /finish로 이동
        if (questionIndex >= dictationQuestions.size()) {
            log.info("[/solve] 모든 문제 풀이 완료 -> /finish 리다이렉트");
            return "redirect:/dictation/finish";
        }

        // 1) 현재 문제만 View로 전달(${currentQuestion.dictationQuestionNo})
        DictationQuestion currentQuestion = dictationQuestions.get(questionIndex);
        model.addAttribute("currentQuestion", currentQuestion);
        log.info("[/solve] 현재 문제번호: {} 정답문장: {})",
            currentQuestion.getDictationQuestionNo(), currentQuestion.getCorrectAnswer());

        // 2) 현재 문제의 tryCount 읽고 남은 횟수(총 2회) 계산 (제출 버튼 부분)
        // 현재 문제 로그 DTO (세션에서 보장됨)
        DictationQuestionLogDto logDto = dictationQuestionLogDtoList.get(questionIndex);

        // 남은 기회 계산: 총 2회 기준
        int tryCount = logDto.getTryCount();
        int triesLeft = Math.max(0, 2 - tryCount);

        // 뷰로 전달(triesLeft는 화면 표시용 1회성 값으로 세션과 db와 관련 없음)
        model.addAttribute("triesLeft", triesLeft);

        log.info("[/solve] 문제번호: {}, tryCount: {}, triesLeft: {}",
            currentQuestion.getDictationQuestionNo(), tryCount, triesLeft);

        return "dictation/solve";
    }

    /**
     * 각 문제 힌트 사용 여부
     * @param questionIndex 세션에 저장된 현재 인덱스
     * @param dictationQuestionLogDtoList 세션에 저장된 이력 dto 목록
     * @return 힌트 사용 여부
     */
    @PostMapping("/use-hint")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> useHint(
        @ModelAttribute("questionIndex") int questionIndex,
        @ModelAttribute("dictationQuestionLogDto") List<DictationQuestionLogDto> dictationQuestionLogDtoList
    ) {
        log.info("[/use-hint] 실행 - questionIndex: {}", questionIndex);

        // 현재 문제만 힌트 사용 처리
        DictationQuestionLogDto dto = dictationQuestionLogDtoList.get(questionIndex);
        dto.setUsedHint("Y");

        log.info("[/use-hint] index: {}, usedHint: Y", questionIndex);
        return ResponseEntityUtils.ok("힌트 사용 완료");
    }

    /**
     * 정답 보기 버튼 누를 시
     * @param questionIndex 세션에 저장된 현재 인덱스
     * @param dictationQuestionLogDtoList 세션에 저장된 이력 dto 목록
     * @return 현재 문제 오답 처리
     */
    @PostMapping("/show-answer")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> showAnswer(
        @ModelAttribute("questionIndex") int questionIndex,
        @ModelAttribute("dictationQuestionLogDto") List<DictationQuestionLogDto> dictationQuestionLogDtoList
    ) {
        log.info("[@PostMapping(/show-answer)] showAnswer 실행 questionIndex: {}", questionIndex);

        // 정답 보기 사용시 오답 처리, 시도횟수 : 2회, 제출문장: <정답 보기 사용>
        DictationQuestionLogDto dictationQuestiondto = dictationQuestionLogDtoList.get(questionIndex);
        dictationService.insertShowAnswerAndSkip(dictationQuestiondto);

        // 변경 후 값 로그 출력
        log.info("[/show-answer] 변경 후 - tryCount: {}, isSuccess: {}, userAnswer: {} / nextIndex: {}",
            dictationQuestiondto.getTryCount(), dictationQuestiondto.getIsSuccess(), dictationQuestiondto.getUserAnswer(), questionIndex + 1);

        return ResponseEntityUtils.ok("정답보기 처리 완료");
    }

    /**
     * 받아쓰기 정답 제출
     * @param userAnswer 사용자 제출 문장
     * @param skip 기본 : "0", 정답보기 누를 시 : "1"
     * @param dictationQuestionLogDtoList 세션에 저장된 이력 dto 목록
     * @param dictationQuestions 세션에 저장된 받아쓰기 문제 목록
     * @param questionIndex 세션에 저장된 현재 인덱스
     * @param model 현재 페이지 세션 갱신용
     * @return /dictation/solve로 리다이렉트 (다음 문제 또는 동일 문제 재도전)
     */
    @PostMapping("/submit-answer")
    public String submitAnswer(@RequestParam("userAnswer") String userAnswer,
        @RequestParam("skip") String skip,
        @ModelAttribute("dictationQuestionLogDto") List<DictationQuestionLogDto> dictationQuestionLogDtoList,
        @ModelAttribute("dictationQuestions") List<DictationQuestion> dictationQuestions,
        @ModelAttribute("questionIndex") int questionIndex,
        Model model,
        RedirectAttributes redirectAttributes) {
        log.info(" [@PostMapping(/submit-answer)] submitAnswer 실행 questionIndex: {}, userAnswer: {}", questionIndex, userAnswer);

        // 정답보기 누를 시 바로 다음 문제로 이동
        if ("1".equals(skip)) {
            DictationQuestionLogDto dictationQuestiondto = dictationQuestionLogDtoList.get(questionIndex);
            dictationService.insertShowAnswerAndSkip(dictationQuestiondto);
            model.addAttribute("questionIndex", questionIndex + 1);
            return "redirect:/dictation/solve";
        }

        // 인덱스가 범위를 벗어나면 즉시 결과 페이지로 이동(제출 문장 제출 시 인덱스)
        if (questionIndex < 0 || questionIndex >= dictationQuestions.size()) {
            log.warn("index 범위 초과 -> /finish 이동");
            return "redirect:/dictation/finish";
        }

        // 제출 문장 공백 방지 (@RequestParam("userAnswer") : null 값 방지)
        if (userAnswer == null) userAnswer = "";

        // 1) 현재 문제 로그 객체
        DictationQuestionLogDto logDto = dictationQuestionLogDtoList.get(questionIndex);
        log.info("현재 문제: {}", questionIndex + 1);

        // 2) 사용자 답안/시도수 갱신
        logDto.setUserAnswer(userAnswer);
        logDto.setTryCount(logDto.getTryCount() + 1);

        // 3) 정답 판정
        String correctAnswer = dictationQuestions.get(questionIndex).getCorrectAnswer();
        boolean isCorrect = dictationService.insertIsCorrectAnswer(userAnswer, correctAnswer);
        logDto.setIsSuccess(isCorrect ? "Y" : "N");
        log.info("[/submit-answer] 정답문장: {}, 제출 문장: {}, 정답 여부: {}, 시도 횟수: {}",
            correctAnswer, userAnswer , isCorrect ? "Y" : "N", logDto.getTryCount());

        // 4) 다음 인덱스: 시도 횟수가 2회이상이거나 정답 여부가: Y일때 questionIndex + 1 아니면 questionIndex
        int nextIndex = (isCorrect || logDto.getTryCount() >= 2) ? questionIndex + 1 : questionIndex;
        // 세션 갱신: questionIndex을 nextIndex로 변경
        model.addAttribute("questionIndex", nextIndex);
        log.info("[/submit-answer] 다음 index: {}", nextIndex);

        // 다음 문제로 넘어가기 전 알림을 띄우기 위한 플래시 세팅
        // 정답일때
        if (isCorrect) {
            log.info("[/submit-answer] 정답 판정으로 플래시 correct=true으로 세팅");
            redirectAttributes.addFlashAttribute("correct", true);
        }

        // 2번째 시도 오답일때
        if (!isCorrect && logDto.getTryCount() >= 2) {
            log.info("[/submit-answer] 2차 시도 후 오답 판정으로 플래시 secondFail=true으로 세팅");
            redirectAttributes.addFlashAttribute("secondFail", true);
        }

        return "redirect:/dictation/solve";
    }

    /**
     * 사용자의 받아쓰기 풀이가 완료되었을 때 호출되는 메서드 받아쓰기 세트 번호를 생성하고, 사용자의 각 문제 풀이 이력을 저장하며, 최종 점수와 결과를 저장한 후 세션
     * 상태를 초기화하고 결과 페이지로 이동합니다.
     * @param securityUser 현재 로그인한 사용자 정보
     * @param dictationQuestions 세션에 저장된 받아쓰기 문제 목록
     * @param dictationQuestionLogDtoList 세션에 저장된 이력 dto 목록
     * @param startDate 시작 시각
     * @param sessionStatus 세션 상태를 초기화하기 위한 Spring 객체
     * @return 결과 페이지로 리디렉션("/dictation/result")
     */
        @GetMapping("/finish")
        public String finishDictation(@AuthenticationPrincipal SecurityUser securityUser,
            @ModelAttribute("dictationQuestions") List<DictationQuestion> dictationQuestions,
            @ModelAttribute("dictationQuestionLogDto") List<DictationQuestionLogDto> dictationQuestionLogDtoList,
            @SessionAttribute("startDate") Date startDate,
            SessionStatus sessionStatus) {
            log.info(" [@GetMapping(/finish)] finishDictation 시작");

            // 1) 사용자/세션 생성
            int userNo = securityUser.getUser().getUserNo();
            int sessionNo = dictationService.createDictationSession(userNo);
            log.info("[/finish]쪽에서 세션 생성 완료: sessionNo: {}", sessionNo);

            // 2) 문제별 이력 저장
            dictationService.insertDictationQuestionLogDto(userNo, sessionNo, dictationQuestions, dictationQuestionLogDtoList);

            // 3) 세트 결과(시간 포함) 저장
            Date endDate = new Date();
            dictationService.insertDictationSessionResult(sessionNo, userNo, startDate, endDate);
            log.info("[/finish]에서 세트 결과 저장 완료 sessionNo: {},userNo: {}, startDate: {}, endDate: {})",sessionNo, userNo, startDate, endDate);

            // 4) 세션 정리
            sessionStatus.setComplete();
            log.info("[/finish] 쪽에서 세션 초기화 완료");

            return "redirect:/dictation/result?dictationSessionNo=" + sessionNo;
        }

    /**
     * 받아쓰기 결과 페이지로 이동되어 그 세트 번호의 결과가 보여지게 함
     * @param dictationSessionNo 문제 세트 번호
     * @param model 받아쓰기 결과 페이지에 보여질 결과값들
     * @return 받아쓰기 결과 페이지
     */
    @GetMapping("/result")
    public String resultPage(@RequestParam("dictationSessionNo") int dictationSessionNo,
        Model model) {
        log.info("받아쓰기 결과 페이지 이동 - sessionNo: {}", dictationSessionNo);

        DictationSession session = dictationService.getDictationSessionByDictationSessionNo(dictationSessionNo);

        // result.html로 전달할 값들
        model.addAttribute("startTime", session.getStartDate());
        model.addAttribute("endTime", session.getEndDate());
        model.addAttribute("score", session.getCorrectScore());
        model.addAttribute("correctCount", session.getCorrectCount());
        model.addAttribute("hintUsedCount", session.getHintUsedCount());

        return "dictation/result";
    }

    /**
     * 로그인한 사용자의 전체 받아쓰기 세트 결과 목록을 조회하여 뷰에 전달
     * @param securityUser 현재 로그인한 사용자 정보
     * @param model 결과 목록이 담길 모델
     * @return 결과 목록을 보여주는 HTML (result-list.html)
     */
    @GetMapping("/results")
    public String showAllResults(@AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        int userNo = securityUser.getUser().getUserNo();
        log.info("사용자 번호: {}", userNo);
        List<DictationSession> results = dictationService.getResultsByUserNo(userNo);
        if (results == null) {
            results = new ArrayList<>();
        }
        log.info("받아쓰기 결과 조회 완료 - 총 {}개 세트 반환", results.size());
        model.addAttribute("results", results);
        log.info("results: {}", results);
        return "dictation/result-list";
    }
}
