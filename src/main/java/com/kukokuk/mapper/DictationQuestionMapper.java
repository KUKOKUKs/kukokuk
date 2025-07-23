package com.kukokuk.mapper;

import com.kukokuk.vo.DictationQuestion;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DictationQuestionMapper {

  /**
   * 받아쓰기 문제를 생성한다.
   * @param dictationQuestion 문제 번호
   */
  void  insertDictationQuestion(DictationQuestion dictationQuestion);

  /**
   * 세트 번호로 문제 전체 가져오기
   * @param dictationSessionNo 세트 번호
   * @return 문제 리스트
   */
  List<DictationQuestion> getQuestionsBySessionNo(int dictationSessionNo);

  /**
   * 특정 받아쓰기 문제 가져오기
   * @param dictationQuestionNo 문제 번호
   * @return DictationQuestion 특정 문제 가져오기
   */
  DictationQuestion getDictationQuestionByNo(int dictationQuestionNo);

  /**
   * 랜덤 문제 10개 가져오기
   * @return 10개의 문제 가져오기
   */
  List<DictationQuestion> getRandomQuestions();

  /**
   * 받아쓰기 문제 수정
   * @param dictationQuestion 문제
   */
  void updateDictationQuestion(DictationQuestion dictationQuestion);

  /**
   * 받아쓰기 문제 삭제
   * @param dictationQuestionNo 문제
   */
  void deleteDictationQuestion(int dictationQuestionNo);
}
