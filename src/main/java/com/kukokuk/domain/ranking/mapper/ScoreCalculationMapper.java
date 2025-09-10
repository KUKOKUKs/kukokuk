package com.kukokuk.domain.ranking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 점수 계산을 위한 Mapper
 *
 * 각 컨텐츠별 점수 계산 + 랭킹 저장을 담당
 * 각 컨텐츠 담당자가 자신의 영역에서 구현
 */
@Mapper
public interface ScoreCalculationMapper {

    /**
     * 스피드 퀴즈 점수 계산 후 랭킹 등록
     * 새로운 랭킹 등록시에만 사용 (중복 체크는 Service에서 처리)
     *
     * @param sessionNo 퀴즈 세션 번호
     * @param userNo 사용자 번호
     * @param groupNo 그룹 번호 (NULL: 전체랭킹)
     * @return 처리된 행 수
     */
    int insertSpeedQuizRanking(
        @Param("sessionNo") int sessionNo,
        @Param("userNo") int userNo,
        @Param("groupNo") Integer groupNo
    );

    /**
     * 스피드 퀴즈 점수 계산 후 랭킹 수정
     * 기존 랭킹 수정시에만 사용 (더 높은 점수일 때만)
     *
     * @param sessionNo 퀴즈 세션 번호
     * @param rankNo 기존 랭킹 번호
     * @return 처리된 행 수
     */
    int updateSpeedQuizRanking(
        @Param("sessionNo") int sessionNo,
        @Param("rankNo") int rankNo
    );

    /*
        받아쓰기 담당자 구현 예정:
        - int insertDictationRanking(int dictationSessionNo, int userNo, Integer groupNo);
        - int updateDictationRanking(int dictationSessionNo, int rankNo);
        - 각자의 점수 계산 공식을 SQL로 구현
    */

    /*
        학습 담당자 구현 예정:
        - int insertStudyRanking(int dailyStudyLogNo, int userNo, Integer groupNo);
        - int updateStudyRanking(int dailyStudyLogNo, int rankNo);
        - 각자의 점수 계산 공식을 SQL로 구현
    */

    /*
        스무고개 담당자 구현 예정:
        - int insertTwentyRanking(int twentyRoomNo, int userNo, Integer groupNo);
        - int updateTwentyRanking(int twentyRoomNo, int rankNo);
        - 각자의 점수 계산 공식을 SQL로 구현
    */

    /*
        서술형 담당자 구현 예정:
        - int insertEssayRanking(int essayQuizLogNo, int userNo, Integer groupNo);
        - int updateEssayRanking(int essayQuizLogNo, int rankNo);
        - 각자의 점수 계산 공식을 SQL로 구현
    */

}