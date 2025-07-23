package com.kukokuk.mapper;

import com.kukokuk.vo.QuizMaster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 퀴즈 마스터(문제) 관련 DB 연동 Mapper
 */
@Mapper
public interface QuizMasterMapper {

  /**
   * 퀴즈의 갯수를 조회한다.
   * @return int
   */
  int getQuizCountByUsageCount(int usageCount);
  /**
   * 특정 유형에서 USAGE_COUNT가 주어진 값 미만인 퀴즈 개수 반환
   * @param questionType 문제 유형 ("뜻", "단어")
   * @param usageCount 기준값
   * @return 해당 조건에 맞는 퀴즈 개수
   */
  int getQuizCountByTypeAndUsageCount(@Param("questionType") String questionType,
      @Param("usageCount") int usageCount);
  /**
   * 하나의 퀴즈를 등록한다.
   * @param quiz 생성할 퀴즈 객체
   */
  void insertQuiz(QuizMaster quiz);


}

