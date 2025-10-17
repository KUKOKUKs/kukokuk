package com.kukokuk.domain.study.mapper;

import com.kukokuk.domain.study.dto.TeacherDailyStudyResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupStudyMapper {
    /**
     * 해당 그룹의 전체 학습자료를 반환 (교사 화면)
     * @param groupNo
     * @return
     */
    List<TeacherDailyStudyResponse> getTeacherDailyStudiesByGroupNo(int groupNo);

    /**
     * 로그를 기준으로 학습을 완료한 사용자의 수 조회
     * @param dailyStudyNo
     * @return
     */
    int countCompletedStudents(int dailyStudyNo);

    /**
     * 로그를 기준으로 해당 일일학습의 서술형 퀴즈를 완료한 사용자의 수 조회
     * @param dailyStudyNo
     * @return
     */
    int countEssayCompletedStudents(int dailyStudyNo);

    /**
     * 현재 그룹 학습자료의 마지막 sequence를 반환
     * @param groupNo
     * @return
     */
    int getMaxSequenceByGroupNo(int groupNo);
}
