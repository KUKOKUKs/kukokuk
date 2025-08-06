package com.kukokuk.mapper;

import com.kukokuk.vo.DailyQuestUser;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailyQuestUserMapper {

    /**
     * 특정 사용자의 일일 도전과제 수행 목록을 조건에 따라 조회
     *
     * @param userNo    사용자번호
     * @param condition 조회 조건 map - "completedDate" : Date - "contentType" : String ("STUDY",
     *                  "QUIZ", "DICTATION")
     * @return 조회된 도전과제 수행 정보(DailyQuestUser) 리스트
     */
    List<DailyQuestUser> getDailyQuestUsersByUserNo(@Param("userNo") int userNo,
        @Param("condition") Map<String, Object> condition);

    /**
     * 식별자로 사용자 도전과제 수행 정보 조회
     *
     * @param dailyQuestUserNo 일일 도전과제 수행 식별자
     * @return 조회된 도전과제 수행 정보
     */
    DailyQuestUser getDailyQuestUserByNo(int dailyQuestUserNo);

    /**
     * 특정 일일도전과제 수행 정보의 IS_OBTAINED를 "Y"로 변경
     *
     * @param dailyQuestUserNo 일일 도전과제 수행 식별자
     */
    void updateIsObtained(int dailyQuestUserNo);
}
