package com.kukokuk.controller;

import com.kukokuk.dto.AnswerRequest;
import com.kukokuk.dto.DictationAnswerDto;
import com.kukokuk.service.DictationService;
import com.kukokuk.vo.DictationQuestion;
import com.kukokuk.vo.DictationSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dictation")
public class DictationController {

  private final DictationService dictationService;


  /**
   * 받아쓰기 시작 페이지 요청 시, 사용자가 아직 풀지 않은 문제 10개를 랜덤으로 가져와 View에 전달
   * @param userNo 사용자 번호
   * @param model 문제(view에 전달용)
   * @return 받아쓰기 시작 페이지
   */
  @GetMapping("/start")
  public String startDictation(SecurityUser securityUser, Model model) {
    List<DictationQuestion> questions =
        dictationService.getRandomDictationQuestionsExcludeUser(securityUser.getUser().getUserNo(), 10);
    model.addAttribute("questions", questions);
    return "dictation/start";
  }


  /**
   * 받아쓰기 문제 결과 확인용 받아쓰기 세트 가져오기
   * @param dictationSessionNo 문제 세트 번호
   * @param model 받아쓰기 세트를 담을 모델
   * @return 받아쓰기 문제 풀이 결과 화면
   */
  @GetMapping("/result")
  public String resultDictationSession(@RequestParam("dictationSessionNo") int dictationSessionNo, Model model) {
    DictationSession session = dictationService.getDictationSessionByNo(dictationSessionNo);
    model.addAttribute("session", session);
    return "dictation/result";
  }

}
