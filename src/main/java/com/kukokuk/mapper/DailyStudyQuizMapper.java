package com.kukokuk.mapper;

import com.kukokuk.vo.DailyStudyQuiz;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyStudyQuizMapper {

  void insertDailyStudyQuiz(DailyStudyQuiz dailyStudyQuiz);
}
