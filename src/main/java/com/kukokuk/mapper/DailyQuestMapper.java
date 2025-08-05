package com.kukokuk.mapper;

import com.kukokuk.vo.DailyQuest;
import com.kukokuk.vo.DailyQuestUser;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailyQuestMapper {

    /**
     * 특정 컨텐츠 타입의 일일도전과제 목록을 조회
     * @param contentType 컨텐츠타입 ("STUDY", "QUIZ", "DICTATION")
     * @return 조회된 일일 도전과제(DailyQuest) 리스트
     */
    List<DailyQuest> getDailyQuestByContentType(String contentType);
}
