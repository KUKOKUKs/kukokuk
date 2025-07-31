package com.kukokuk.mapper;

import com.kukokuk.vo.DailyStudyEssayQuiz;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyStudyEssayQuizMapper {

  void insertdailyStudyEssayQuiz(DailyStudyEssayQuiz dailyStudyEssayQuiz);
}
