package com.kukokuk.controller;

import com.kukokuk.dto.AnswerRequest;
import com.kukokuk.dto.DictationAnswerDto;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.DictationService;
import com.kukokuk.vo.DictationQuestion;
import com.kukokuk.vo.DictationSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Log4j2
@Controller
@SessionAttributes({"dictationQuestions", "answerMap"})
@RequiredArgsConstructor
@RequestMapping("/dictation")
public class DictationController {

  private final DictationService dictationService;

  @ModelAttribute("dictationQuestions")
  public List<DictationQuestion> initQuestions() {
    return new ArrayList<>();
  }

  @ModelAttribute("answerMap")
  public Map<Integer, String> initAnswerMap() {
    return new HashMap<>();
  }

  /**
   * 받아쓰기 시작 페이지 요청 시, 사용자가 아직 풀지 않은 문제 10개를 랜덤으로 가져와 View에 전달 (삭제 예정)
   * @param securityUser 사용자
   * @param model 문제(view에 전달용)
   * @return 받아쓰기 시작 페이지
   */
  @GetMapping("/start1")
  public String startDictation(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
    log.info("startDictation() 컨트롤러 실행");
    int userNo = securityUser.getUser().getUserNo();
    log.info("start dictation userNo: {}", userNo);
    List<DictationQuestion> questions =
        dictationService.getRandomDictationQuestionsExcludeUser(userNo, 10);
    model.addAttribute("questions", questions);
    for (DictationQuestion q : questions) {
      log.info("questions info: {}", q.getCorrectAnswer());
    }
    return "dictation/start1";
  }

  /**
   * 받아쓰기 시작 요청을 처리하는 메서드
   * 현재 로그인한 사용자의 번호를 바탕으로, 아직 풀지 않은 받아쓰기 문제 10개를 랜덤으로 가져와
   * 세션에 저장하고, 첫 번째 문제부터 풀 수 있도록 문제 풀이 페이지로 이동
   *
   * @param securityUser 현재 로그인한 사용자 정보
   * @param questionList 세션에 저장되는 받아쓰기 문제 목록
   * @param model View로 전달할 데이터 저장 객체
   * @return 문제 풀이 페이지("dictation/solve")로 이동
   */
  @GetMapping("/start")
  public String startDictation(@AuthenticationPrincipal SecurityUser securityUser,
      @ModelAttribute("dictationQuestions") List<DictationQuestion> questionList,
      Model model) {

    log.info("startDictation(/start) 실행");

    // 1. 현재 로그인한 사용자의 번호 조회
    int userNo = securityUser.getUser().getUserNo();
    log.info("userNo: {}", userNo);

    // 2. 사용자가 아직 풀지 않은 문제 중에서 랜덤으로 10개 가져오기
    List<DictationQuestion> questions = dictationService.getRandomDictationQuestionsExcludeUser(
        userNo, 10);

    // 3. 기존 문제 리스트 초기화하고 새 문제 10개 추가
    questionList.clear();
    questionList.addAll(questions);

    log.info("랜덤 문제 10개 설정 완료");
    for (int i = 0; i < questions.size(); i++) {
      log.info("문제 {}: {}", i + 1, questions.get(i).getCorrectAnswer());
    }

    // 4. index(현재 문제)는 1부터 시작하도록 설정
    model.addAttribute("currentIndex", 1);
    return "dictation/solve";
  }

  /**
   * 사용자가 특정 문제를 풀 수 있도록 해당 인덱스에 해당하는 받아쓰기 문제를 조회하고,
   * 문제 내용과 기존에 입력한 답안을 View에 전달합니다.
   * 만약 index 문제 개수를 초과하면 받아쓰기가 끝났으므로 자동으로 결과 처리 페이지로 리디렉션
   *
   * @param index 현재 풀 문제의 번호
   * @param questionList 세션에 저장된 받아쓰기 문제 목록
   * @param answerMap 세션에 저장된 사용자 답안
   * @param model View에 전달할 데이터 저장 객체
   * @return 문제 풀이 페이지("dictation/solve") 또는 결과 처리 리디렉션("/dictation/finish")
   */
  @GetMapping("/solve")
  public String showQuestion(@RequestParam("index") int index,
      @ModelAttribute("dictationQuestions") List<DictationQuestion> questionList,
      @ModelAttribute("answerMap") Map<Integer, String> answerMap,
      Model model) {

    log.info("showProblem 실행 - index: {}", index);

    // 1. index가 문제 수(10개)를 초과하면 받아쓰기가 끝나므로 finish로 리디렉션
    if (index > questionList.size()) {
      log.info("문제 수 초과로 인해 finish로 반환 ");
      return "redirect:/dictation/finish";
    }

    // 2. 현재 문제 가져오기 List는 index가 0이고 현재 문제 index는 1로 설정했기 때문에 (index - 1)
    DictationQuestion currentQuestion = questionList.get(index - 1);
    log.info("현재 문제: {}", currentQuestion.getCorrectAnswer());

    model.addAttribute("question", currentQuestion);
    model.addAttribute("index", index);
    model.addAttribute("answer", answerMap.getOrDefault(index, "")); // null값이 안 들어가려고 answerMap.getOrDefault(index, "") 사용
    return "dictation/solve";
  }

  /**
   * 사용자가 입력한 받아쓰기 답안을 세션에 저장한 후, 다음 문제로 이동하도록 리디렉션합니다.
   * 답안은 세션 내의 answerMap에 문제 번호(index)를 키로 하여 임시 저장
   * @param index 현재 문제 번호
   * @param userAnswer 사용자가 입력한 답안
   * @param answerMap 세션에 저장된 사용자 답안
   * @return 다음 문제 페이지로 리디렉션("/dictation/solve?index=다음번호")
   */
  @PostMapping("/submit-answer")
  public String submitAnswer(@RequestParam("index") int index,
      @RequestParam("userAnswer") String userAnswer,
      @ModelAttribute("answerMap") Map<Integer, String> answerMap) {

    log.info("submitAnswer 실행 - index: {}, userAnswer: {}", index, userAnswer);

    // 1. 사용자가 입력한 답안을 세션 answerMap에 저장 (세션에 임시 저장)
    answerMap.put(index, userAnswer);
    log.info("answerMap 저장됨: {}", answerMap);

    // 2. 다음 문제(index + 1)로 이동하도록 리디렉션
    return "redirect:/dictation/solve?index=" + (index + 1);
  }

  /**
   * 사용자의 받아쓰기 풀이가 완료되었을 때 호출되는 메서드
   * 받아쓰기 세트 번호를 생성하고, 사용자의 각 문제 풀이 이력을 저장하며,
   * 최종 점수와 결과를 저장한 후 세션 상태를 초기화하고 결과 페이지로 이동합니다.
   * @param securityUser 현재 로그인한 사용자 정보
   * @param dictationQuestion 세션에 저장된 받아쓰기 문제 목록
   * @param answerMap 세션에 저장된 사용자 답안
   * @param sessionStatus 세션 상태를 초기화하기 위한 Spring 객체
   * @return 결과 페이지로 리디렉션("/dictation/result")
   */
  @GetMapping("/finish")
  public String finish(@AuthenticationPrincipal SecurityUser securityUser,
      @ModelAttribute("dictationQuestions") List<DictationQuestion> dictationQuestion,
      @ModelAttribute("answerMap") Map<Integer, String> answerMap,
      SessionStatus sessionStatus) {

    log.info("finish 실행");

    // 1. 사용자 번호 가져오기
    int userNo = securityUser.getUser().getUserNo();
    log.info("userNo: {}", userNo);

    // 2. 빈 받아쓰기 세트 생성
    int sessionNo = dictationService.createDictationSession(userNo);
    log.info("세션 생성 완료 - sessionNo: {}", sessionNo);

    // 3. 받아쓰기 이력 저장
    for (int i = 0; i < dictationQuestion.size(); i++) {
      DictationQuestion q = dictationQuestion.get(i);
      int index = i + 1; // answerMap에 저장된 key는 1부터 시작하는 indexd이다
      String userAnswer = answerMap.get(index);  // index로 꺼내기

      String usedHint = "N"; // 힌트 사용 여부(일단 "N" 수정필요)

      // 로그 출력: 세트 번호는 항상 동일하게 sessionNo 사용
      log.info("문제세트번호 {} 저장 - 문제번호: {}, 사용자 입력: {}", sessionNo, q.getDictationQuestionNo(), userAnswer);

      // 제출 서비스 호출 (사용자 번호, 세트번호, 문제번호, 사용자답안, 힌트 사용)
      dictationService.submitAnswer(userNo, sessionNo, q.getDictationQuestionNo(), userAnswer, usedHint);
    }

    // 4. 받아쓰기 세트 결과 저장(맞춘 개수, 점수 계산, 힌트 사용 여부 등)
    dictationService.saveDictationSessionResult(sessionNo, userNo);
    log.info("세션 결과 반영 완료 - sessionNo: {}", sessionNo);

    // 5. 세션 초기화
    sessionStatus.setComplete();
    log.info("세션 상태 초기화 완료");

    return "redirect:/dictation/result";
  }

  @GetMapping("/result")
  public String resultPage() {
    log.info("받아쓰기 결과 페이지 이동");
    return "dictation/result";
  }
}
