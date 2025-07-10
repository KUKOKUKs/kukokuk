package com.kukokuk.mapper;

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
     *                  - "order" : String ("createdDate")
     * @return 조회된 사용자의 일일학습 이력 리스트
     */
    public List<DailyStudyLog> getDailyStudyLogsByUserNo(@Param("userNo") int userNo,
        @Param("condition") Map<String, Object> condition);

    /**
     * 특정 사용자에 맞춤화된 일일학습 자료 목록을 조회
     * @param userNo 사용자 번호
     * @param studyDifficulty 사용자의 학습 수준
     *                        
     * @param condition 조회 조건
     *                  - "rows" : int 조회 행 개수
     * @return 조회된 사용자의 일일학습 자료 리스트 
     */
    public List<DailyStudy> getDailyStudiesByUser(@Param("userNo") int userNo,
        @Param("studyDifficulty") int studyDifficulty,
        @Param("condition") Map<String, Object> condition);

}
