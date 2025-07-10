package com.kukokuk.mapper;

import com.kukokuk.vo.DailyQuest;
import com.kukokuk.vo.DailyQuestUser;
import com.kukokuk.vo.DailyStudy;
import com.kukokuk.vo.DailyStudyLog;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyQuestMapper {

    /**
     * 특정 컨텐츠 타입의 일일도전과제 목록을 조회
     * @param contentType 컨텐츠타입 ("STUDY", "QUIZ", "DICTATION")
     * @return 조회된 일일 도전과제(DailyQuest) 리스트
     */
    List<DailyQuest> getDailyQuestByContentType(String contentType);

    /**
     * 특정 사용자의 도전과제 수행 목록을 조건에 따라 조회
     * @param userNo 사용자번호
     * @param condition 조회 조건 map
     *                  - "completedDate" :
     *                  - "contentType" : String ("STUDY", "QUIZ", "DICTATION")
     * @return 조회된 도전과제 수행 정보(DailyQuestUser) 리스트
     */
    List<DailyQuestUser> getDailyQuestUserByUserNo(int userNo, Map<String, Object> condition);
}
