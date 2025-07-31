package com.kukokuk.mapper;

import com.kukokuk.vo.DictationQuestion;
import com.kukokuk.vo.DictationQuestionLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DictationQuestionMapper {

  /**
   * 받아쓰기 문제를 생성한다.
   * @param dictationQuestion 문제 번호
   */
  void insertDictationQuestion(DictationQuestion dictationQuestion);

  /**
   * 랜덤으로 받아쓰기 문제 조회
   * @param count 문제 개수
   * @return 랜덤으로 가져온 받아쓰기 문제
   */
  List<DictationQuestion> getRandomDictationQuestions(int count);

  /**
   * 사용자가 푼 문제 목록 조회 (풀었던 문제만)
   * @param userNo 사용자 번호
   * @return 사용자가 푼 DictationQuestion 리스트
   */
  List<DictationQuestion> getSolvedQuestionsByUserNo(int userNo);

  /**
   * 랜덤 문제 가져오기
   * @param limit 문제 가져올 개수
   * @return 문제 가져오기
   */
  List<DictationQuestion> getRandomQuestions(int limit);

  /**
   * 받아쓰기 문제 번호로 정답문장 가져오기
   * @param dictationQuestionNo 문제 번호
   * @return 정답 문장
   */
  String getCorrectAnswerByQuestionNo(int dictationQuestionNo);

  /**
   * 받아쓰기 문제 수정
   * @param dictationQuestion 받아쓰기 문제
   */
  void updateDictationQuestion(DictationQuestion dictationQuestion);

  /**
   * 받아쓰기 문제 삭제
   * @param dictationQuestionNo 문제 번호
   */
  void deleteDictationQuestion(int dictationQuestionNo);

  /**
   * 사용자에게 아직 출제되지 않은 받아쓰기 문제 중에서
   * 랜덤하게 지정한 개수만큼 문제를 조회하는 메서드
   *
   * @param userNo 문제를 푼 이력이 있는 사용자 번호
   * @param count 조회할 문제 개수
   * @return 사용자가 풀지 않은 문제 리스트
   */
  List<DictationQuestion> getRandomDictationQuestionsExcludeUser(
      @Param("userNo") int userNo,
      @Param("count") int count
  );
}
