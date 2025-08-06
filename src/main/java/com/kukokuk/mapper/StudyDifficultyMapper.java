package com.kukokuk.mapper;

import com.kukokuk.vo.StudyDifficulty;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudyDifficultyMapper {

    // 학습수준 번호로 학습수준 모든 컬럼을 조회
    StudyDifficulty getDifficultyByNo(int studyDifficultyNo);
}
