package com.kukokuk.dto;

import com.kukokuk.vo.DailyStudy;
import com.kukokuk.vo.DailyStudyLog;
import com.kukokuk.vo.DailyStudyMaterial;
import com.kukokuk.vo.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

/**
 * 학습자료의 원본데이터, 수준에 맞게 재구성된 학습자료, 특정 사용자의 학습 이력을 담는 DTO
 */
@Getter
@Setter
@Alias("UserStudyRecommendationDto")
@ToString
public class UserStudyRecommendationDto {
    // null이 담길 수 있도록 int대신 Integer 로 설정
    private Integer dailyStudyMaterialNo;
    private Integer dailyStudyNo;
    private Integer dailyStudyLogNo;
    private Integer userNo;
    private Integer dailyStudyEssayQuizLogNo;

    private DailyStudyMaterial dailyStudyMaterial;
    private DailyStudy dailyStudy;
    private DailyStudyLog dailyStudyLog;
}
