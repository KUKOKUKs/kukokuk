package com.kukokuk.mapper;

import com.kukokuk.vo.DictationQuestionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DictationQuestionLogMapper {

  /**
   * 받아쓰기 이력 생성
   * @param dictationQuestionLog 받아쓰기 이력
   */
  void insertLog(DictationQuestionLog dictationQuestionLog);

  /**
   * 식별자 받아쓰기 힌트 사용 여부
   * @param dictationQuestionLogNo 식별자
   * @param userHint 힌트 사용 여부
   */
  void updateHintUsed(@Param("dictationQuestionLogNo") int dictationQuestionLogNo, @Param("userHint") String userHint);

  /**
   * 식별자 받아쓰기 정답 여부
   * @param dictationQuestionLogNo 식별자
   * @param isSuccess 정답 여부
   */
  void updateIsSuccess(@Param("dictationQuestionLogNo") int dictationQuestionLogNo, @Param("isSuccess") String isSuccess);

  /**
   * 식별자 받아쓰기 시도 횟수
   * @param dictationQuestionLogNo 식별자
   */
  void updateTryCount(@Param("dictationQuestionLogNo") int dictationQuestionLogNo);

  /**
   * 받아쓰기 문제 세트 맞은 문제 개수 조회
   * @param dictationSessionNo 받아쓰기 문제 세트 번호
   * @return 맞은 문제 개수
   */
  int getcountCorrectAnswers(@Param("dictationSessionNo") int dictationSessionNo);

  /**
   * 받아쓰기 문제 세트 사용한 힌트 수 조회
   * @param dictationSessionNo 받아쓰기 문제 세트 번호
   * @return 사용한 힌트 개수
   */
  int getcountHintsUsed(@Param("dictationSessionNo") int dictationSessionNo);


}
