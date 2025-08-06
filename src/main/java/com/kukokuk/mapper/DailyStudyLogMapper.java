package com.kukokuk.mapper;

import com.kukokuk.vo.DailyStudyLog;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailyStudyLogMapper {

    /**
     * 특정 사용자의 일일학습 이력 목록을 조회
     *
     * @param userNo    사용자번호
     * @param condition 조회 조건 - "rows" : int 조회 행 개수 - "offset" : int 오프셋 - "order" : String
     *                  ("updatedDate") 정렬기준 - "status" : String ("inProgress","completed") 학습상태
     * @return 조회된 사용자의 일일학습 이력 리스트
     */
    public List<DailyStudyLog> getStudyLogsByUserNo(@Param("userNo") int userNo,
        @Param("condition") Map<String, Object> condition);

    /**
     * 특정 사용자의 특정 학습 자료에 대한 이력을 조회
     *
     * @param userNo       사용자번호
     * @param dailyStudyNo 학습자료번호
     * @return 조회된 사용자의 해당 학습자료의 일일학습 이력
     */
    DailyStudyLog getStudyLogByUserNoAndDailyStudyNo(@Param("userNo") int userNo,
        @Param("dailyStudyNo") int dailyStudyNo);

    /**
     * 사용자의 학습 이력을 생성
     *
     * @param dailyStudyLog
     */
    void createStudyLog(DailyStudyLog dailyStudyLog);

    /**
     * 학습 이력 번호로 학습이력 조회
     *
     * @param dailyStudyLogNo
     * @return
     */
    DailyStudyLog getStudyLogByNo(int dailyStudyLogNo);

    /**
     * 학습이력을 수정
     *
     * @param updateLog
     */
    void updateStudyLog(DailyStudyLog updateLog);
}
