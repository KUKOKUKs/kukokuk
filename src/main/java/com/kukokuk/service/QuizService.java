package com.kukokuk.service;

import com.kukokuk.mapper.DictEntryMapper;
import com.kukokuk.mapper.QuizMasterMapper;
import com.kukokuk.vo.DictEntry;
import com.kukokuk.vo.QuizMaster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizService {

  private final QuizMasterMapper quizMasterMapper;
  private final DictEntryMapper dictEntryMapper;
  /**
   * 사전 데이터에서 무작위 단어 1개로 퀴즈를 생성하고 저장한다.
   */
  public void insertQuizByWordRandomEntry(int count) {
    List<DictEntry> correctEntries = dictEntryMapper.getRandomDictEntries(count);

    List<String> correctWords = new ArrayList<>();
    for (DictEntry entry : correctEntries) {
      correctWords.add(entry.getWord());
    }

    int limit = correctEntries.size() * 3;
    List<DictEntry> optionEntries = dictEntryMapper.getRandomWordExclude(correctWords, limit);


    for (int i = 0; i < correctEntries.size(); i++) {
      DictEntry correct = correctEntries.get(i);

      // optionEntries에서 i * 3부터 i * 3 + 3까지 3개 가져오기
      List<DictEntry> options = optionEntries.subList(i * 3, i * 3 + 3);

      options.add(correct);
      Collections.shuffle(options);
      int correctIndex = -1;
      for (int j = 0; j < options.size(); j++) {
        if (options.get(j).getEntryNo() == correct.getEntryNo()) {
          correctIndex = j + 1;
          break;
        }
      }
      QuizMaster quiz = new QuizMaster();
      quiz.setEntryNo(correct.getEntryNo());
      System.out.println("getEntryNo" + correct.getEntryNo());
      quiz.setQuestion(correct.getDefinition());
      quiz.setOption1(options.get(0).getWord());
      quiz.setOption2(options.get(1).getWord());
      quiz.setOption3(options.get(2).getWord());
      quiz.setOption4(options.get(3).getWord());
      quiz.setSuccessAnswer(correctIndex);
      quiz.setQuestionType("뜻");

      quizMasterMapper.insertQuiz(quiz);
      correctIndex = 0;
    }
  }

  public void insertQuizByDefRandomEntry(int count) {
    List<DictEntry> correctEntries = dictEntryMapper.getRandomDictEntries(count);

    List<String> correctWords = new ArrayList<>();
    for (DictEntry entry : correctEntries) {
      correctWords.add(entry.getWord());
    }

    int limit = correctEntries.size() * 3;
    List<DictEntry> optionEntries = dictEntryMapper.getRandomWordExclude(correctWords, limit);

    for (int i = 0; i < correctEntries.size(); i++) {
      DictEntry correct = correctEntries.get(i);

      // optionEntries에서 i * 3부터 i * 3 + 3까지 3개 가져오기
      List<DictEntry> options = optionEntries.subList(i * 3, i * 3 + 3);

      options.add(correct);
      Collections.shuffle(options);
      int correctIndex = -1;
      for (int j = 0; j < options.size(); j++) {
        if (options.get(j).getEntryNo() == correct.getEntryNo()) {
          correctIndex = j + 1;
          break;
        }
      }
      QuizMaster quiz = new QuizMaster();
      quiz.setEntryNo(correct.getEntryNo());
      System.out.println("getEntryNo" + correct.getEntryNo());
      quiz.setQuestion(correct.getWord());
      quiz.setOption1(options.get(0).getDefinition());
      quiz.setOption2(options.get(1).getDefinition());
      quiz.setOption3(options.get(2).getDefinition());
      quiz.setOption4(options.get(3).getDefinition());
      quiz.setSuccessAnswer(correctIndex);
      quiz.setQuestionType("단어");

      quizMasterMapper.insertQuiz(quiz);
      correctIndex = 0;
    }
  }


  /**
   * 사전 데이터의 랜덤한 단어를 기반으로 서로 다른 유형의 퀴즈를 100개씩 생성한다.
   *
   * @param count 생성할 퀴즈 수
   */
  public void insertRandomQuizBulk(int count) {
      try {
          insertQuizByWordRandomEntry(count);
          insertQuizByDefRandomEntry(count);
      } catch (Exception e) {
        System.out.println("퀴즈 생성 도중 실패: " + e.getMessage());
      }
  }

  public void insertRandomTypeQuizBulk(int usageCount) {
    final int targetCount = 100;

    // 뜻 유형
    int meaningCount = quizMasterMapper.getQuizCountByTypeAndUsageCount("뜻",usageCount);
    int meaningToCreate = Math.max(0, targetCount - meaningCount);
    if (meaningToCreate > 0) {
      insertQuizByWordRandomEntry(meaningToCreate);
      System.out.println("뜻 퀴즈 " + meaningToCreate + "개 생성 완료");
    }

    // 단어 유형
    int wordCount = quizMasterMapper.getQuizCountByTypeAndUsageCount("단어", usageCount);
    int wordToCreate = Math.max(0, targetCount - wordCount);
    if (wordToCreate > 0) {
      insertQuizByDefRandomEntry(wordToCreate);
      System.out.println("단어 퀴즈 " + wordToCreate + "개 생성 완료");
    }
  }


}