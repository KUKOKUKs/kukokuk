package com.kukokuk.controller;

import com.kukokuk.dto.DictationAnswerDto;
import com.kukokuk.dto.DictationSessionRequestDto;
import com.kukokuk.service.DictationService;
import com.kukokuk.vo.DictationQuestion;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dictation")
public class ApiDictationController {

  private final DictationService dictationService;

  /**
   * 받아쓰기 문제를 사용자에게 제공하는 API
   * - 사용자가 아직 풀지 않은 받아쓰기 문제를 랜덤으로 10개 반환
   * - 만약 사용자가 푼 문제로 인해 10개를 채우지 못하면, 부족한 개수만큼 문제를 새로 생성한 후 다시 가져옴
   *
   * @param userNo 사용자 식별 번호
   * @return 받아쓰기 문제 리스트 (최대 10개)
   */
  @GetMapping("/questions")
  public List<DictationQuestion> getQuestions(@RequestParam("userNo") int userNo) {
    List<DictationQuestion> questions = dictationService.getDictationQuestionsByUserNo(userNo);
    return questions; // JSON 형식으로 자동 변환되어 응답됨
  }

  /**
   *
   * 받아쓰기 문제의 정답을 제출하는 API
   * 클라이언트에서 문제 풀이 결과(세트 번호, 문제 번호, 제출 답안)를 전송하면
   * 서비스 로직을 통해 DB에 제출 결과를 저장하거나 업데이트
   * 요청 형식 (JSON):
   * {
   *  "sessionNo": 1,
   *  "questionNo": 5,
   *  "userAnswer": "사용자 입력 정답"
   * }
   * @param answerDto 사용자가 제출한 답안을 담은 DTO 객체
   * @return 성공 응답(200)
   */
  @PostMapping("/submit-answer")
  public ResponseEntity<Void> submitAnswer(@RequestBody DictationAnswerDto answerDto) {
    dictationService.submitAnswer(
        answerDto.getDictationSessionNo(),
        answerDto.getDictationQuestionNo(),
        answerDto.getUserAnswer()
    );
    return ResponseEntity
          .ok()
          .build();
  }

  /**
   * 받아쓰기 세트 결과 저장 API
   * 클라이언트에서 받아쓰기 세트 번호(dictationSessionNo)와 사용자 번호(userNo)를 포함한 요청을 전송하면,
   * 서비스에서 세션 결과를 집계하여 저장
   * 요청 형식 (JSON):
   * {
   *   "dictationSessionNo": 123,
   *   "userNo": 1
   * }
   * @param requestDto 받아쓰기 세트 번호와 사용자 번호를 담은 DTO 객체
   * @return 성공 응답(200)
   */
  @PostMapping("/session/save")
  public ResponseEntity<Void> saveSession(@RequestBody DictationSessionRequestDto requestDto) {
    dictationService.saveDictationSessionResult(
        requestDto.getDictationSessionNo(),
        requestDto.getUserNo()
    );
    return ResponseEntity
          .ok()
          .build();
  }
}
