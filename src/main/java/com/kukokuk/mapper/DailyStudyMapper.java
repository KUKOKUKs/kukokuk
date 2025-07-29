package com.kukokuk.mapper;

import com.kukokuk.dto.UserStudyRecommendationDto;
import com.kukokuk.vo.DailyStudy;
import com.kukokuk.vo.DailyStudyLog;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailyStudyMapper {

    /**
     * 특정 사용자의 일일학습 이력 목록을 조회
     *
     * @param userNo 사용자번호
     * @param condition 조회 조건
     *                  - "rows" : int 조회 행 개수
     *                  - "offset" : int 오프셋
     *                  - "order" : String ("updatedDate") 정렬기준
     *                  - "status" : String ("inProgress","completed") 학습상태
     * @return 조회된 사용자의 일일학습 이력 리스트
     */
    public List<DailyStudyLog> getDailyStudyLogsByUserNo(@Param("userNo") int userNo,
        @Param("condition") Map<String, Object> condition);

    /**
     * 특정 사용자에 맞춤화된 일일학습 자료 목록을 조회
     * @param userNo 사용자 번호
     * @param userInfo 사용자의 수준/진도 정보
     *                 - "studyDifficulty" : int 사용자 수준 (1~6)
     *                 - "currentSchool" : String 사용자 진도 학교 (초, 중)
     *                 - "currentGrade" : int 사용자 학년
     * @param condition 조회 조건
     *                  - "rows" : int 조회 행 개수
     * @return 조회된 사용자의 일일학습 자료 리스트
     *         + 사용자가 학습중인 학습자료면 이력까지 함께 조회
     */
    public List<UserStudyRecommendationDto> getDailyStudiesByUser(@Param("userNo") int userNo,
        @Param("userInfo") Map<String, Object> userInfo,
        @Param("condition") Map<String, Object> condition);

    // 학습자료를 생성
  void insertDailyStudy(DailyStudy dailyStudy);
}
