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
  public void insertQuizByRandomEntry() {
    DictEntry correctEntry = dictEntryMapper.getRandomOne();

    if (correctEntry == null) {
      throw new IllegalStateException("정답 단어를 찾을 수 없습니다.");
    }
    List<DictEntry> candidates = dictEntryMapper.getRandomEntriesExclude(correctEntry.getEntryNo(), 3);
    if (candidates.size() < 3) {
      throw new IllegalStateException("오답 후보가 부족합니다.");
    }
    candidates.add(correctEntry);
    Collections.shuffle(candidates);
    int correctIndex = -1;
    for (int i = 0; i < candidates.size(); i++) {
      if (candidates.get(i).getEntryNo() == correctEntry.getEntryNo()) {
        correctIndex = i + 1;
        break;
      }
    }
    QuizMaster quiz = new QuizMaster();
    quiz.setEntryNo(correctEntry.getEntryNo());
    System.out.println("getEntryNo" + correctEntry.getEntryNo());
    quiz.setQuestion(correctEntry.getDefinition());
    quiz.setOption1(candidates.get(0).getWord());
    quiz.setOption2(candidates.get(1).getWord());
    quiz.setOption3(candidates.get(2).getWord());
    quiz.setOption4(candidates.get(3).getWord());
    quiz.setSuccessAnswer(correctIndex);
    quiz.setQuestionType("뜻");
    quiz.setDifficulty(null);

    quizMasterMapper.insertQuiz(quiz);
  }


  /**
   * 사전 데이터를 기반으로 랜덤 퀴즈 N개를 자동 생성하고 저장한다.
   *
   * @param count 생성할 퀴즈 수
   */
  public void insertRandomQuizBulk(int count) {
    for (int i = 0; i < count; i++) {
      try {
        insertQuizByRandomEntry();
      } catch (Exception e) {
        System.out.println("[" + i + "] 번째 퀴즈 생성 실패: " + e.getMessage());
      }
    }

  }
  public int getQuizCount(){
      return quizMasterMapper.getQuizCounter();
  }
}