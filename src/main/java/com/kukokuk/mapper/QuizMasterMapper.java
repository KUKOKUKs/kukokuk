package com.kukokuk.mapper;

import com.kukokuk.vo.QuizMaster;
import org.apache.ibatis.annotations.Mapper;

/**
 * 퀴즈 마스터(문제) 관련 DB 연동 Mapper
 */
@Mapper
public interface QuizMasterMapper {

  /**
   * 퀴즈의 갯수를 조회한다.
   * @return int
   */
  int getQuizCounter();

  /**
   * 하나의 퀴즈를 등록한다.
   * @param quiz 생성할 퀴즈 객체
   */
  void insertQuiz(QuizMaster quiz);
}
