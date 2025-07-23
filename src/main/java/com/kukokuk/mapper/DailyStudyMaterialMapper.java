package com.kukokuk.mapper;

import com.kukokuk.vo.DailyStudyMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailyStudyMaterialMapper {

  // DailyStudyMaterial을 생성
  void insertStudyMaterial(DailyStudyMaterial dailyStudyMaterial);

  // 해당 학교/ 학년의 가장 큰 시퀀스 값 조회
  int getMaxSequenceBySchoolAndGrade(@Param("school") String school, @Param("grade") int grade);
}
