package com.kukokuk.mapper;

import com.kukokuk.dto.DailyQuestProgressAggDto;
import com.kukokuk.vo.DailyQuest;
import com.kukokuk.vo.DailyQuestUser;
import com.kukokuk.vo.ExpLogs;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailyQuestMapper {

    /*
     * 경험치 이력 인터페이스 추가 예정
     * 퀘스트 내용에 맞는 조건이 완료되었다면
     * 퀘스트 완료 내역 테이블에 추가 insert
     * 이미 완료가된 내역이 있다면 더이상 추가하지 않음
     */

    /**
     * 사용자의 퀘스트 완료 내역 수정(보상 수령 여부)
     * 식별자, 사용자번호에 대한 조건처리가 필요하여
     * DailyQuestUser 객체로 처리
     * @param dailyQuestUser 사용자의 퀘스트 완료 내역 정보
     */
    void updateDailyQuestUserObtained(DailyQuestUser dailyQuestUser);

    /**
     * 퀘스트 완료 내역 등록
     * @param dailyQuestUser 퀘스트 완료 내역(퀘스트번호, 사용자번호)
     */
    void insertDailyQuestUser(DailyQuestUser dailyQuestUser);

    /**
     * 획득한 경험치 정보 등록
     * @param expLogs 획득한 경험치 정보(사용자번호, 컨텐츠정보, 경험치)
     */
    void insertExpLog(ExpLogs expLogs);

    /**
     * 모든 퀘스트 정보 목록 조회
     * @return 퀘스트 정보 목록
     */
    List<DailyQuest> getDailyQuests();

    /**
     * 사용자 번호로 오늘에 대한 경험치, 횟수 집계(CONTENT_TYPE 기준)
     * @param userNo 사용자 정보
     * @return 오늘 경험치, 횟수 집계
     */
    List<DailyQuestProgressAggDto> getDailyQuestProgressAggByUserNo(int userNo);

    /**
     * 사용자의 일일도전과제 완료된 목록 조회
     * @param userNo 사용자 번호
     * @return 사용자의 일일도전과제 완료된 목록
     */
    List<DailyQuestUser> getDailyQuestUserByUserNo(int userNo);

    /**
     * 퀘스트 완료 내역 번호로 사용자의 오늘 퀘스트 완료 내역 조회
     * @param dailyQuestUserNo 퀘스트 완료 내역 번호
     * @param userNo 사용자 번호
     * @return 퀘스트 완료 내역
     */
    DailyQuestUser getDailyQuestUserByDailyQuestUserNo(@Param("dailyQuestUserNo") int dailyQuestUserNo
        , @Param("userNo") int userNo);

    /** 삭제 예정
     * 특정 컨텐츠 타입의 일일도전과제 목록을 조회
     * @param contentType 컨텐츠타입 ("STUDY", "QUIZ", "DICTATION")
     * @return 조회된 일일 도전과제(DailyQuest) 리스트
     */
    List<DailyQuest> getDailyQuestByContentType(String contentType);

}
