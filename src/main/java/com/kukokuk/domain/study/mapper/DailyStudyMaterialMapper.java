package com.kukokuk.domain.study.mapper;

import com.kukokuk.domain.study.vo.DailyStudyMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailyStudyMaterialMapper {

    // DailyStudyMaterial을 생성
    void insertStudyMaterial(DailyStudyMaterial dailyStudyMaterial);

    // 해당 학교/ 학년의 가장 큰 시퀀스 값 조회
    int getMaxSequenceBySchoolAndGrade(@Param("school") String school, @Param("grade") int grade);

    // 식별자로 학습자료 원본데이터 조회
    DailyStudyMaterial getStudyMaterialByNo(int dailyStudyMaterialNo);

    // DailyStudyMaterial을 수정
    void updateStudyMaterial(DailyStudyMaterial material);
}
