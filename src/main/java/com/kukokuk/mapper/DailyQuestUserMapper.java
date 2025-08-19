package com.kukokuk.mapper;

import com.kukokuk.vo.DailyQuestUser;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailyQuestUserMapper {

    /**
     * 사용자의 퀘스트 완료 내역 수정(보상 수령 여부)
     * 식별자, 사용자번호에 대한 조건처리가 필요하여
     * DailyQuestUser 객체로 처리
     * @param dailyQuestUser 사용자의 퀘스트 완료 내역 정보
     */
    void updateDailyQuestUserObtained(DailyQuestUser dailyQuestUser);

    /**
     * 사용자 번호와 퀘스트 번호로 오늘 퀘스트 완료 내역 조회
     * @param dailyQuestNo 퀘스트 번호
     * @param userNo 사용자 번호
     * @return 오늘 퀘스트 완료 내역 정보
     */
    DailyQuestUser getDailyQuestUserByQuestNoAndUserNo(@Param("dailyQuestNo") int dailyQuestNo
        , @Param("userNo") int userNo);

    /**
     * 퀘스트 완료 내역 번호로 사용자의 오늘 퀘스트 완료 내역 조회
     * @param dailyQuestUserNo 퀘스트 완료 내역 번호
     * @param userNo 사용자 번호
     * @return 퀘스트 완료 내역
     */
    DailyQuestUser getDailyQuestUserByDailyQuestUserNo(@Param("dailyQuestUserNo") int dailyQuestUserNo
        , @Param("userNo") int userNo);

    /**
     * 사용자의 일일도전과제 완료된 목록 조회
     * @param userNo 사용자 번호
     * @return 사용자의 일일도전과제 완료된 목록
     */
    List<DailyQuestUser> getDailyQuestUserByUserNo(int userNo);

    /**
     * 퀘스트 완료 내역 등록
     * @param dailyQuestUser 퀘스트 완료 내역
     */
    void insertDailyQuestUser(DailyQuestUser dailyQuestUser);

    /* ********************* 아래 삭제 예정 ********************* */

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

    /**
     * 이 쿼리를 수행하는 날짜에 존재하는 사용자의 해당 도전과제에 대한 이력 조회
     */
    DailyQuestUser getTodayQuestUserByUserNoAndQuestNo(@Param("userNo") int userNo,
        @Param("dailyQuestNo") int dailyQuestNo);
}
