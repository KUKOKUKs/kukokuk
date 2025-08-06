package com.kukokuk.mapper;

import com.kukokuk.vo.DailyStudyQuiz;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyStudyQuizMapper {

    void insertDailyStudyQuiz(DailyStudyQuiz dailyStudyQuiz);

    /**
     * 해당 학습자료에 속하는 학습 퀴즈 목록 조회
     */
    List<DailyStudyQuiz> getStudyQuizzesByDailyStudyNo(int dailyStudyNo);

    /**
     * 학습퀴즈 번호로 학습퀴즈 조회
     */
    DailyStudyQuiz getStudyQuizByNo(int studyQuizNo);
}
