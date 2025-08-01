package com.kukokuk.mapper;

import com.kukokuk.vo.DailyStudyQuizLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailyStudyQuizLogMapper {

  /**
   * 사용자의 해당 학습자료에 속한 퀴즈에 대한 이력 정보 조회
   */
  List<DailyStudyQuizLog> getStudyQuizLogsByUserNoAndDailyStudyNo(@Param("userNo") int userNo, @Param("dailyStudyNo") int dailyStudyNo);

  /**
   * 사용자의 해당 학습퀴즈에 대한 이력 조회
   */
  DailyStudyQuizLog getStudyQuizLogsByUserNoAndStudyQuizNo(@Param("userNo") int userNo, @Param("studyQuizNo") int studyQuizNo);

  /**
   * 학습퀴즈 이력을 생성
   */
  void createStudyQuizLog(DailyStudyQuizLog dailyStudyQuizLog);

  /**
   * 학습퀴즈 이력 번호로 학습퀴즈이력 조회
   */
  DailyStudyQuizLog getStudyQuizLogsByNo(int dailyStudyQuizLogNo);

  /**
   * 학습퀴즈 수정
   */
  void updateStudyQuizLog(DailyStudyQuizLog log);
}
