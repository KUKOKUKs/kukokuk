package com.kukokuk.service;

import com.kukokuk.mapper.DictEntryMapper;
import com.kukokuk.mapper.QuizMasterMapper;
import com.kukokuk.vo.DictEntry;
import com.kukokuk.vo.QuizMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

  private final QuizMasterMapper quizMasterMapper;
  private final DictEntryMapper dictEntryMapper;

  /**
   * 특정 사전 엔트리 기반으로 퀴즈 1개 생성 및 저장
   *
   * @param entryNo 사전 단어 번호
   */
  public void insertQuizByEntryNo(int entryNo) {
    // 정답 단어 가져오기
    DictEntry correctEntry = dictEntryMapper.selectEntryByNo(entryNo);
    String question = correctEntry.getDefinition();

    // 오답용 단어 3개 추출
    List<DictEntry> candidates = dictEntryMapper.selectRandomEntriesExclude(entryNo, 3);
    candidates.add(correctEntry); // 정답 포함
    Collections.shuffle(candidates); // 무작위 섞기

    // 퀴즈 생성
    QuizMaster quiz = new QuizMaster();
    quiz.setEntryNo(entryNo);
    quiz.setQuestion(question);
    quiz.setQuestionType("뜻");
    quiz.setOption1(candidates.get(0).getWord());
    quiz.setOption2(candidates.get(1).getWord());
    quiz.setOption3(candidates.get(2).getWord());
    quiz.setOption4(candidates.get(3).getWord());

    for (int i = 0; i < 4; i++) {
      if (candidates.get(i).getWord().equals(correctEntry.getWord())) {
        quiz.setSuccessAnswer(i + 1);
        break;
      }
    }

    quizMasterMapper.insertQuiz(quiz);
  }
}
