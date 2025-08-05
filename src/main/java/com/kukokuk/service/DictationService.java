package com.kukokuk.service;

import com.kukokuk.ai.GeminiClient;
import com.kukokuk.mapper.DictationQuestionLogMapper;
import com.kukokuk.mapper.DictationQuestionMapper;
import com.kukokuk.mapper.DictationSessionMapper;
import com.kukokuk.response.DictationQuestionLogResponse;
import com.kukokuk.response.DictationSessionResultResponse;
import com.kukokuk.vo.DictationQuestion;
import com.kukokuk.vo.DictationQuestionLog;
import com.kukokuk.vo.DictationSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class DictationService {

  private final DictationQuestionMapper dictationQuestionMapper;
  private final DictationQuestionLogMapper dictationQuestionLogMapper;
  private final DictationSessionMapper dictationSessionMapper;
  private final GeminiClient geminiClient;

  /**
   * Gemini를 통해 문장을 받아와서 힌트들을 생성하고 DB에 저장하는 메소드
   * @param count 생성할 문장 수
   */
  @Transactional
  public void generateAiQuestions(int count) {
    // [1] Gemini에게 받아쓰기용 짧고 쉬운 문장들을 요청하는 프롬프트 생성
    String prompt = "받아쓰기 문제로 사용할 짧고 쉬운 한국어 문장 " + count + "개를 만들어줘. 각 문장은 줄바꿈(\\n)으로 구분해줘.";

    // [2] Gemini로부터 생성된 문장 결과를 받아옴
    String response = geminiClient.getGeminiResponse(prompt);

    // [3] 줄바꿈(\n)을 기준으로 각 문장을 배열로 분리
    String[] sentences = response.split("\n");

    // 분리된 문장들을 하나씩 처리
    for (String raw : sentences) {
      // 앞뒤 공백 제거
      String sentence = raw.trim();

      // [1] 빈 문자열은 스킵
      if (sentence.isEmpty()) continue;

      // [2] 설명 문장 필터링
      if (sentence.contains("준비했습니다") || sentence.matches(".*\\d+개.*")) continue;

      // [3] "1. " 같은 앞번호 제거
      sentence = sentence.replaceFirst("^\\d+\\.\\s*", "");

      // [4] 문장 끝 마침표 제거
      sentence = sentence.replaceFirst("\\.$", "");

      // [5] 다시 확인
      if (sentence.isEmpty()) continue;

      DictationQuestion question = new DictationQuestion();
      question.setCorrectAnswer(sentence);
      question.setHint1(getUnderlineHint(sentence));
      question.setHint2(getInitialConsonantsHint(sentence));
      question.setHint3(getFirstCharHint(sentence));

      dictationQuestionMapper.insertDictationQuestion(question);
    }
  }

  /**
   * 띄어쓰기 힌트 생성 ("__ __   __ __ __" 형식)
   */
  private String getUnderlineHint(String sentence) {
    StringBuilder hint = new StringBuilder();
    for (char c : sentence.toCharArray()) {
      if (c == ' ') {
        hint.append("   ");
      } else {
        hint.append("__ ");
      }
    }
    return hint.toString().trim();
  }

  /**
   * 초성 힌트 생성 ("ㄴㄴㅇㄴㅎㄱㅇㄱㅌ" 형식)
   */
  private String getInitialConsonantsHint(String sentence) {
    StringBuilder result = new StringBuilder();
    for (char ch : sentence.toCharArray()) {
      if (ch >= 0xAC00 && ch <= 0xD7A3) {
        int uniVal = ch - 0xAC00;
        int initialIndex = uniVal / (21 * 28);
        char initial = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ".charAt(initialIndex);
        result.append(initial);
      } else if (ch != ' ') {
        result.append(ch);
      }
    }
    return result.toString();
  }

  /**
   * 첫 글자 힌트 생성 (첫 번째 단어의 첫 글자)
   */
  private String getFirstCharHint(String sentence) {
    for (String word : sentence.split(" ")) {
      if (!word.isEmpty()) return word.substring(0, 1);
    }
    return "";
  }


  /**
   * 사용자에게 제공할 받아쓰기 문제 10개를 가져오기
   * 이미 푼 문제는 제외하며, 부족하면 문제를 새로 생성한 뒤 다시 가져온다
   * @param userNo 사용자 번호
   * @return 받아쓰기 문제 리스트 (총 10개)
   */
  public List<DictationQuestion> getDictationQuestionsByUserNo(int userNo, int count) {

      // 1. 사용자 기준, 푼 문제는 제외하고 10문제 가져오기
      List<DictationQuestion> questions = dictationQuestionMapper.getRandomDictationQuestionsExcludeUser(userNo, 10);

      // 2. 10문제 보다 부족한 문제 수세기 (예: 4개 부족)
      if (questions.size() < count) {
        int toCreate = count - questions.size();

      // 3. 부족한 수 만큼 새로운 문제 생성 (예: 4개 생성)
      this.generateAiQuestions(toCreate);

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
  public void saveDictationSessionResult(int dictationSessionNo, int userNo, Date startDate, Date endDate) {
    // 1. 정답 개수 조회
    int correctCount = dictationQuestionLogMapper.getCountCorrectAnswers(dictationSessionNo);

    // 2. 힌트 사용 횟수 조회
    int hintUsedCount = dictationQuestionLogMapper.getCountHintsUsed(dictationSessionNo);

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
  public void submitAnswer(int userNo, int dictationSessionNo, int dictationQuestionNo, String userAnswer, String usedHint, int tryCount) {

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
      newLog.setTryCount(tryCount);                                                   // 첫 시도
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
  public boolean isCorrectAnswer(String userAnswer, String correctAnswer) {
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

  public List<DictationSession> getResultsByUserNo(int userNo) {
    return dictationSessionMapper.getDictationSessionResultsByUserNo(userNo);
  }

  public List<DictationQuestionLog> getLogsBySessionNo(int dictationSessionNo) {
    List<DictationQuestionLog> logs = dictationQuestionLogMapper.getDictationQuestionLogBySessionNo(dictationSessionNo);

    // 이거 추가하면 됨 (stream도 필요 없음)
    if (logs == null) return new ArrayList<>();

    // 직접 for문으로 null 제거
    List<DictationQuestionLog> cleaned = new ArrayList<>();
    for (DictationQuestionLog log : logs) {
      if (log != null) cleaned.add(log);
    }

    return cleaned;
  }
}
