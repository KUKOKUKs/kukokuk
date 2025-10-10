package com.kukokuk.domain.study.mapper;

import com.kukokuk.domain.study.dto.TeacherDailyStudyResponse;
import com.kukokuk.domain.study.dto.UserStudyRecommendationDto;
import com.kukokuk.domain.study.vo.DailyStudy;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailyStudyMapper {

    /**
     * 특정 사용자에 맞춤화된 일일학습 자료 목록을 조회
     * @param userNo 사용자 번호
     * @param userInfo 사용자의 수준/진도 정보
     *                 - "studyDifficulty" : int 사용자 수준 (1~6)
     *                 - "currentSchool" : String 사용자 진도 학교 (초등, 중등)
     *                 - "currentGrade" : int 사용자 학년
     * @param condition 조회 조건
     *                  - "rows" : int 조회 행 개수
     * @return 조회된 사용자의 일일학습 자료 리스트
     *         + 사용자가 학습중인 학습자료면 이력까지 함께 조회
     */
    List<UserStudyRecommendationDto> getDailyStudiesByUser(@Param("userNo") int userNo,
        @Param("userInfo") Map<String, Object> userInfo,
        @Param("condition") Map<String, Object> condition);

    /**
     * 특정 그룹 학습 자료를 사용자의 이력 상태에 맞게 목록으로 조회
     * @param groupNo 학습자료를 조회할 그룹의 번호
     * @param userNo 사용자 번호
     * @param condition 조회 조건
     *                  - "rows" : int 조회 행 개수
     * @return 조회된 사용자의 일일학습 자료 리스트
     *         + 사용자가 학습중인 학습자료면 이력까지 함께 조회
     */
    List<UserStudyRecommendationDto> getDailyStudiessByGroupAndUser(
        @Param("groupNo") int groupNo,
        @Param("userNo") int userNo,
        @Param("condition") Map<String, Object> condition);

    // 학습자료를 생성
    void insertDailyStudy(DailyStudy dailyStudy);

    /**
    * 학습자료 번호로 학습자료를 조회
    * @param dailyStudyNo 학습자료 번호
    * @return 학습자료
    */
    DailyStudy getDailyStudyByNo(int dailyStudyNo);

    /**
     * 학습 원본데이터 번호와 학습수준으로 학습자료를 조회
     * @param dailyStudyMaterialNo 학습 원본 데이터 번호
     * @param studyDifficultyNo 학습 수준 번호
     * @return 조회된 학습자료
     */
    UserStudyRecommendationDto getDailyStudyByMaterialNoAndDifficulty(
        @Param("dailyStudyMaterialNo") int dailyStudyMaterialNo,
        @Param("studyDifficultyNo") int studyDifficultyNo);

    /**
     * groupNo를 업데이트
     * @param dailyStudyNo
     * @param groupNo
     */
    void updateGroupNo(@Param("dailyStudyNo") int dailyStudyNo, @Param("groupNo") int groupNo);
}
