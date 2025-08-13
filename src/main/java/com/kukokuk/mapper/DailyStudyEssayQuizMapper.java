package com.kukokuk.mapper;

import com.kukokuk.vo.DailyStudyEssayQuiz;
import com.kukokuk.vo.DailyStudyEssayQuizLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailyStudyEssayQuizMapper {

    void insertdailyStudyEssayQuiz(DailyStudyEssayQuiz dailyStudyEssayQuiz);

    // 서술형 퀴즈를 학습자료 번호로 조회
    DailyStudyEssayQuiz getEssayQuizByDailyStudyNo(int dailyStudyNo);

    // 서술형 퀴즈를 식별자 번호로 조화
    DailyStudyEssayQuiz getEssayQuizByNo(int dailyStudyEssayQuizNo);

    // 서술형퀴즈 이력을 서술형퀴즈 번호와 사용자 번호로 조회
    DailyStudyEssayQuizLog getEssayQuizLogByQuizNoAndUserNo(@Param("dailyStudyEssayQuizNo") int dailyStudyEssayQuizNo, @Param("userNo") int userNo);

    // 서술형 퀴즈 이력을 생성
    void insertStudyEssayQuizLog(DailyStudyEssayQuizLog essayQuizLog);
    
    // 서술형 퀴즈 이력을 식별자로 조회
    DailyStudyEssayQuizLog getEssayQuizLogByNo(int dailyStudyEssayQuizLogNo);

    // 사용자 퀴즈 이력을 업데이트
    void updateStudyEssayQuizLog(DailyStudyEssayQuizLog log);
    
}
