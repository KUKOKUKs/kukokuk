package com.kukokuk.controller;

import com.kukokuk.dto.DictationAnswerDto;
import com.kukokuk.dto.DictationSessionRequestDto;
import com.kukokuk.response.ApiResponse;
import com.kukokuk.response.DictationQuestionLogResponse;
import com.kukokuk.response.ResponseEntityUtils;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.DictationService;
import com.kukokuk.vo.DictationQuestion;
import com.kukokuk.vo.DictationQuestionLog;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
   *  - 사용자가 아직 풀지 않은 받아쓰기 문제를 랜덤으로 10개 반환
   *  - 만약 사용자가 푼 문제로 인해 10개를 채우지 못하면, 부족한 개수만큼 문제를 새로 생성한 후 다시 가져옴
   * @param securityUser 사용자 식별 번호
   * @return 받아쓰기 문제 리스트 (최대 10개)
   */
  @GetMapping("/questions")
  public ResponseEntity<ApiResponse<List<DictationQuestion>>> getQuestions(@AuthenticationPrincipal SecurityUser securityUser) {
    log.info("getQuestions() 컨트롤러 실행");
    int userNo = securityUser.getUser().getUserNo();
    log.info("question userNo: {}", userNo);
    List<DictationQuestion> questions = dictationService.getDictationQuestionsByUserNo(userNo,10 );
    for (DictationQuestion q : questions) {
      log.info("questions info: {}", q.getDictationQuestionNo());
    }
    return ResponseEntityUtils.ok(questions); // JSON 형식으로 자동 변환되어 응답됨
  }

  /**
   *
   * 받아쓰기 문제의 정답을 제출하는 API`
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
        answerDto.getUserNo(),
        answerDto.getDictationSessionNo(),
        answerDto.getDictationQuestionNo(),
        answerDto.getUserAnswer(),
        answerDto.getUsedHint(),
        answerDto.getTryCount()
    );
    return ResponseEntity
          .ok()
          .build();
  }


  /**
   * 받아쓰기 세트 번호를 기준으로 해당 세트의 문제 풀이 이력을 조회
   * @param dictationSessionNo 받아쓰기 세트 번호
   * @return 해당 세트에 대한 문제 풀이 이력 목록
   */
  @GetMapping("/results/{sessionNo}/logs")
  public ResponseEntity<ApiResponse<List<DictationQuestionLog>>> getLogs(@PathVariable("sessionNo") int dictationSessionNo) {
    log.info("세트번호: {}", dictationSessionNo);
    List<DictationQuestionLog> logs = dictationService.getLogsBySessionNo(dictationSessionNo);
    log.info("이력 조회 성공 - 총 {}개 로그 반환", logs.size());
    return ResponseEntity.ok(ApiResponse.success(logs));
  }
}
