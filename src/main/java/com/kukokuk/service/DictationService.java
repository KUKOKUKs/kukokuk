package com.kukokuk.service;

import com.kukokuk.mapper.DictationQuestionLogMapper;
import com.kukokuk.mapper.DictationQuestionMapper;
import com.kukokuk.mapper.DictationSessionMapper;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.vo.DictationQuestion;
import com.kukokuk.vo.DictationQuestionLog;
import com.kukokuk.vo.DictationSession;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

@Log4j2
@Service
@RequiredArgsConstructor
public class DictationService {

  private final DictationQuestionMapper dictationQuestionMapper;
  private final DictationQuestionLogMapper dictationQuestionLogMapper;
  private final DictationSessionMapper dictationSessionMapper;


  /**
   * 사용자가 아직 풀지 않은 받아쓰기 문제 중에서 무작위로 count 개수를 가져오기
   * @param userNo 사용자 번호
   * @param count 문제 수
   * @return 사용자가 아직 풀지 않은 받아쓰기 문제 리스트
   */
  public List<DictationQuestion> getRandomDictationQuestionsExcludeUser(int userNo, int count) {
    return dictationQuestionMapper.getRandomDictationQuestionsExcludeUser(userNo, count);
  }

  /**
   * 받아쓰기 문제를 count 개수만큼 생성하여 DB에 저장하는 서비스 메서드
   * @param count 생성할 문제 개수
   */
  public void insertDictationRandomEntry(int count) {
    for (int i = 0; i < count; i++) {
      // 임시
      //List<DictationQuestion> newQuestions = getRandomQuestions(count);

      // 1. 문제 객체 생성 및 값 세팅
     // DictationQuestion question = new DictationQuestion();
      //question.setCorrectAnswer(correctAnswer);
     // question.setHint1(hint1);
     // question.setHint2(hint2);
     // question.setHint3(hint3);

      // 2. DB에 문제 저장
      //dictationQuestionMapper.insertDictationQuestion(question);
    }
  }

  /**
   * 사용자에게 제공할 받아쓰기 문제 10개를 가져오기
   * 이미 푼 문제는 제외하며, 부족하면 문제를 새로 생성한 뒤 다시 가져온다
   * @param userNo 사용자 번호
   * @return 받아쓰기 문제 리스트 (총 10개)
   */
  public List<DictationQuestion> getDictationQuestionsByUserNo(int userNo) {

      // 1. 사용자 기준, 푼 문제는 제외하고 10문제 가져오기
      List<DictationQuestion> questions = dictationQuestionMapper.getRandomDictationQuestionsExcludeUser(userNo, 10);

      // 2. 10문제 보다 부족한 문제 수세기 (예: 4개 부족)
      if (questions.size() < 10) {
        int toCreate = 10 - questions.size();

      // 3. 부족한 수 만큼 새로운 문제 생성 (예: 4개 생성)
      insertDictationRandomEntry(toCreate);

      // 4. 새로 생성한 문제 중에서도 사용자 기준으로 푼 문제는 다시 제외하고 가져오기 (예: 새로 생성된 4개 가져옴)
      List<DictationQuestion> additional = dictationQuestionMapper.getRandomDictationQuestionsExcludeUser(userNo, toCreate);

      // 5. 기존에 가져온 문제 리스트에 추가 (예: 새로 생성된 4개 합쳐서 questions에 집어넣어 10개 만들기)
      questions.addAll(additional);
    }

    return questions;
  }

  /**
   * 받아쓰기 문제 결과 받아쓰기 세트에 저장
   * @param dictationSessionNo 문제 세트 번호
   * @param userNo 회원 번호
   */
  @Transactional
  public void saveDictationSessionResult(int dictationSessionNo, int userNo) {
    // 1. 정답 개수 조회
    int correctCount = dictationQuestionLogMapper.getCountCorrectAnswers(dictationSessionNo);

    // 2. 힌트 사용 횟수 조회
    int hintUsedCount = dictationQuestionLogMapper.getCountHintsUsed(dictationSessionNo);

    // 3. 시작 시간 조회 (가장 이른 문제 풀이 시간)
    Date startDate = new Date();

    // 4. 종료 시간은 현재 시각
    Date endDate = new Date();

    // 5. 점수 계산
    int correctScore = correctCount * 10;

    // 6. 세션 저장
    DictationSession session = new DictationSession();
    session.setDictationSessionNo(dictationSessionNo); // 외부에서 받은 세션 번호 사용
    session.setUserNo(userNo);
    session.setStartDate(startDate);
    session.setEndDate(endDate);
    session.setCorrectCount(correctCount);
    session.setHintUsedCount(hintUsedCount);
    session.setCorrectScore(correctScore);

    dictationSessionMapper.updateDictationSessionResult(session);
  }

  /**
   * 문제 제출 시 이력에 저장
   * @param dictationSessionNo 문제 세트 번호
   * @param dictationQuestionNo 문제 번호
   * @param userAnswer 제출 문장
   */
  @Transactional
  public void submitAnswer(int userNo, int dictationSessionNo, int dictationQuestionNo, String userAnswer, String usedHint) {

    // 1. 정답 문장 가져오기
    String correctAnswer = dictationQuestionMapper.getCorrectAnswerByQuestionNo(dictationQuestionNo);

    // 2. 기존 제출 이력 확인
    DictationQuestionLog existingLog = dictationQuestionLogMapper.getLogBySessionAndQuestion(dictationSessionNo, dictationQuestionNo);

    // 3. 제출 이력 저장
    if (existingLog == null) {
      // 첫 제출 → INSERT
      DictationQuestionLog newLog = new DictationQuestionLog();
      newLog.setUserNo(userNo);                                                       // 사용자 번호
      newLog.setDictationSessionNo(dictationSessionNo);                               // 문제 세트 번호
      newLog.setDictationQuestionNo(dictationQuestionNo);                             // 문제 번호
      newLog.setUserAnswer(userAnswer);                                               // 사용자 제출 답안
      newLog.setTryCount(1);                                                          // 첫 시도
      newLog.setIsSuccess(isCorrectAnswer(userAnswer, correctAnswer) ? "Y" : "N");    // 정답 여부
      newLog.setUsedHint(usedHint);                                                   // 힌트 사용 여부

      dictationQuestionLogMapper.insertDictationQuestionLog(newLog);
    } else {
      // 두 번째 제출 → UPDATE
      existingLog.setUserAnswer(userAnswer);                                           // 새로 제출한 답안
      existingLog.setTryCount(existingLog.getTryCount() + 1);                          // 시도 횟수 증가
      existingLog.setIsSuccess(isCorrectAnswer(userAnswer, correctAnswer) ? "Y" : "N");// 정답 여부 판정
      existingLog.setUsedHint(usedHint);                                               // 힌트 사용 여부 갱신

      dictationQuestionLogMapper.updateDictationQuestionLog(existingLog);
    }
  }


  /**
   * 사용자 제출 문장과 정답 문장을 비교하여 정답 여부를 판별
   * 문장부호 및 특수문자는 제외하고 한글, 숫자, 띄어쓰기만 남긴 후 비교
   * @param userAnswer 제출문장
   * @param correctAnswer 정답문장
   * @return 두 문장이 같으면 true (정답), 다르면 false (오답)
   */
  private boolean isCorrectAnswer(String userAnswer, String correctAnswer) {
    // 제출문장, 정답문장 두 문장 모두 NULL이 아니게 예외처리
    if (userAnswer == null || correctAnswer == null) return false;

    // 받아쓰기 정답 여부 판별을 위해 문장부호 및 특수문자는 제외하고 한글, 숫자, 띄어쓰기만 남김
    String refinedUserAnswer = userAnswer.replaceAll("[^ㄱ-ㅎ가-힣0-9 ]", "");
    String refinedCorrectAnswer = correctAnswer.replaceAll("[^ㄱ-ㅎ가-힣0-9 ]", "");

    // 비교해서 같다(true) 아니면 다르다(false)로 반환
    return refinedUserAnswer.equals(refinedCorrectAnswer);
  }

  /**
   * 받아쓰기 세트 번호로 받아쓰기 세트 가져오기
   * @param dictationSessionNo 문제 세트 번호
   * @return 받아쓰기 세트
   */
  public DictationSession getDictationSessionByNo(int dictationSessionNo) {
    return dictationSessionMapper.getDictationSessionByNo(dictationSessionNo);
  }

  /**
   * 힌트 사용
   * @param dictationQuestionLogNo 식별자
   */
  public void useHint(int dictationQuestionLogNo) {
    dictationQuestionLogMapper.updateHintUsed(dictationQuestionLogNo, "Y");
  }

  /**
   * 받아쓰기 문제 수정
   * @param dictationQuestion 문제
   */
  public void updateDictationQuestion(DictationQuestion dictationQuestion) {
    dictationQuestionMapper.updateDictationQuestion(dictationQuestion);
  }

  /**
   * 받아쓰기 문제 삭제
   * @param dictationQuestionNo 문제 번호
   */
  public void deleteDictationQuestion(int dictationQuestionNo) {
    dictationQuestionMapper.deleteDictationQuestion(dictationQuestionNo);
  }

  /**
   * 사용자의 번호를 기반으로 받아쓰기 세트 객체를 생성하고,
   * 시작일자와 종료일자를 현재 시각으로 설정한 후 DB에 저장
   * @param userNo 사용자 번호
   * @return 생성된 받아쓰기 세트
   */
  public int createDictationSession(int userNo) {
    DictationSession session = new DictationSession();
    session.setUserNo(userNo);
    session.setStartDate(new Date());
    session.setEndDate(new Date());
    dictationSessionMapper.insertDictationSession(session);
    return session.getDictationSessionNo(); // MyBatis에서 자동 채번되어 들어간다고 가정
  }


}
