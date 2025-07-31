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
}
