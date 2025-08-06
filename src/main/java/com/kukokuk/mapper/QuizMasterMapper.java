package com.kukokuk.mapper;

import com.kukokuk.vo.QuizMaster;
import java.util.List;
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
  int getQuizCount();

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

  /**
   * usage_count가 지정된 값 이하인 퀴즈 중 랜덤하게 limit 개 조회
   * @param usageCount 풀린횟수
   * @param limit 기준값
   * @return 퀴즈
   */
  List<QuizMaster> getQuizMastersForSpeed(@Param("usageCount") int usageCount, @Param("limit") int limit);


  /**
   * 특정 퀴즈 번호의 정답 번호를 조회한다.
   * @param quizNo 퀴즈 번호
   * @return 정답 선택 번호
   */
  Integer getCorrectChoiceByQuizNo(int quizNo);
}

