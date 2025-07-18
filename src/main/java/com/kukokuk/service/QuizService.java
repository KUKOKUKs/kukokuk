package com.kukokuk.service;

import com.kukokuk.mapper.DictEntryMapper;
import com.kukokuk.mapper.QuizMasterMapper;
import com.kukokuk.vo.DictEntry;
import com.kukokuk.vo.QuizMaster;
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

    int limit = correctEntries.size() * 3;
    List<DictEntry> optionEntries = dictEntryMapper.getRandomEntriesExclude(correctEntries, limit);

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

    int limit = correctEntries.size() * 3;
    List<DictEntry> optionEntries = dictEntryMapper.getRandomEntriesExclude(correctEntries, limit);



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
   * 사전 데이터를 기반으로 랜덤 퀴즈 N개를 자동 생성하고 저장한다.
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
  //생성된 전체 퀴즈 갯수체크함수
  public int getQuizCount(int usageCount){
      return quizMasterMapper.getQuizCounter(usageCount);
  }
}