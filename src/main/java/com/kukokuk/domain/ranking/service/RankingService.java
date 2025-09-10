package com.kukokuk.domain.ranking.service;

import com.kukokuk.common.exception.AppException;
import com.kukokuk.domain.ranking.mapper.RankingMapper;
import com.kukokuk.domain.ranking.vo.Ranking;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 랭킹 관련 서비스
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingMapper rankingMapper;

    /**
     * 새로운 랭킹 등록 (첫 플레이)
     * @param contentType 컨텐츠 타입
     * @param score 계산된 점수
     * @param userNo 사용자 번호
     */
    @Transactional
    public void insertRanking(String contentType, double score, int userNo) {
        log.info("insertRanking 서비스 실행 - 사용자: {}, 컨텐츠: {}, 점수: {}", userNo, contentType, score);

        try {
            Ranking ranking = new Ranking();
            ranking.setContentType(contentType);
            ranking.setPlayCount(1);
            ranking.setTotalScore(BigDecimal.valueOf(score));
            ranking.setUserNo(userNo);

            rankingMapper.insertRanking(ranking);
            log.info("신규 랭킹 등록 완료 - 랭킹번호: {}", ranking.getRankNo());
        } catch (DataAccessException e) {
            log.error("랭킹 등록 실패", e);
            throw new AppException("랭킹을 등록하지 못했습니다.");
        }
    }

    /**
     * 기존 랭킹 업데이트 (재플레이 시 누적 평균 계산)
     * @param rankNo 기존 랭킹 번호
     * @param newScore 새로운 점수
     */
    @Transactional
    public void updateRanking(int rankNo, double newScore) {
        log.info("updateRanking 서비스 실행 - 랭킹번호: {}, 새점수: {}", rankNo, newScore);

        try {
            // 기존 랭킹 정보 조회
            Ranking existingRanking = rankingMapper.getRankingByRankNo(rankNo);
            if (existingRanking == null) {
                log.error("랭킹 정보를 찾을 수 없음: {}", rankNo);
                throw new AppException("랭킹 정보를 찾을 수 없습니다.");
            }

            // 누적 평균 계산
            int currentPlayCount = existingRanking.getPlayCount();
            double currentTotalScore = existingRanking.getTotalScore().doubleValue();

            // 새로운 누적 절대값 = (현재 평균 점수 × 현재 플레이 횟수) + 새 점수
            double newCumulativeScore = (currentTotalScore * currentPlayCount) + newScore;
            int newPlayCount = currentPlayCount + 1;

            // 새로운 평균 점수 = 누적 절대값 / 새로운 플레이 횟수
            double newAverageScore = newCumulativeScore / newPlayCount;

            log.info("평균 점수 계산: {}회차 {:.2f} + 새점수 {:.2f} = 평균 {:.2f}",
                currentPlayCount, currentTotalScore, newScore, newAverageScore);

            // 랭킹 업데이트
            existingRanking.setPlayCount(newPlayCount);
            existingRanking.setTotalScore(BigDecimal.valueOf(newAverageScore));

            rankingMapper.updateRanking(existingRanking);
            log.info("랭킹 업데이트 완료");
        } catch (DataAccessException e) {
            log.error("랭킹 업데이트 실패", e);
            throw new AppException("랭킹을 업데이트하지 못했습니다.");
        }
    }

    /**
     * 사용자의 특정 컨텐츠 랭킹 조회
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @return 랭킹 정보, 없으면 null
     */
    public Ranking getRankingByUserAndContent(int userNo, String contentType) {
        log.info("getRankingByUserAndContent 서비스 실행 - 사용자: {}, 컨텐츠: {}", userNo, contentType);

        try {
            return rankingMapper.getRankingByUserAndContent(userNo, contentType);
        } catch (DataAccessException e) {
            log.error("랭킹 조회 실패", e);
            throw new AppException("랭킹을 조회하지 못했습니다.");
        }
    }

    /**
     * 전체 랭킹 목록 조회
     * @param contentType 컨텐츠 타입
     * @param limit 조회할 개수
     * @return 전체 랭킹 목록 (점수 높은 순)
     */
    public List<Ranking> getGlobalRankings(String contentType, int limit) {
        log.info("getGlobalRankings 서비스 실행 - 컨텐츠: {}, 개수: {}", contentType, limit);

        try {
            return rankingMapper.getGlobalRankings(contentType, limit);
        } catch (DataAccessException e) {
            log.error("전체 랭킹 조회 실패", e);
            throw new AppException("랭킹을 조회하지 못했습니다.");
        }
    }

    /**
     * 그룹 내 랭킹 목록 조회
     * @param contentType 컨텐츠 타입
     * @param groupNo 그룹 번호
     * @param limit 조회할 개수
     * @return 그룹 내 랭킹 목록 (점수 높은 순)
     */
    public List<Ranking> getGroupRankings(String contentType, int groupNo, int limit) {
        log.info("getGroupRankings 서비스 실행 - 컨텐츠: {}, 그룹: {}, 개수: {}", contentType, groupNo, limit);

        try {
            return rankingMapper.getGroupRankings(contentType, groupNo, limit);
        } catch (DataAccessException e) {
            log.error("그룹 랭킹 조회 실패", e);
            throw new AppException("랭킹을 조회하지 못했습니다.");
        }
    }

    /**
     * 전체 랭킹에서 사용자 순위 조회
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @return 전체 순위 (1부터 시작)
     */
    public int getGlobalUserRanking(int userNo, String contentType) {
        log.info("getGlobalUserRanking 서비스 실행 - 사용자: {}, 컨텐츠: {}", userNo, contentType);

        try {
            return rankingMapper.getGlobalUserRanking(userNo, contentType);
        } catch (DataAccessException e) {
            log.error("사용자 순위 조회 실패", e);
            throw new AppException("순위를 조회하지 못했습니다.");
        }
    }

    /**
     * 그룹 내 랭킹에서 사용자 순위 조회
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @param groupNo 그룹 번호
     * @return 그룹 내 순위 (1부터 시작)
     */
    public int getGroupUserRanking(int userNo, String contentType, int groupNo) {
        log.info("getGroupUserRanking 서비스 실행 - 사용자: {}, 컨텐츠: {}, 그룹: {}", userNo, contentType, groupNo);

        try {
            return rankingMapper.getGroupUserRanking(userNo, contentType, groupNo);
        } catch (DataAccessException e) {
            log.error("그룹 내 사용자 순위 조회 실패", e);
            throw new AppException("순위를 조회하지 못했습니다.");
        }
    }

    /**
     * 컨텐츠별 통합 랭킹 처리 메서드
     * 기존 랭킹이 있으면 업데이트, 없으면 신규 등록
     * @param contentType 컨텐츠 타입
     * @param score 점수
     * @param userNo 사용자 번호
     */
    @Transactional
    public void processRanking(String contentType, double score, int userNo) {
        log.info("processRanking 서비스 실행 - 사용자: {}, 컨텐츠: {}, 점수: {}", userNo, contentType, score);

        try {
            // 기존 랭킹 조회
            Ranking existingRanking = getRankingByUserAndContent(userNo, contentType);

            if (existingRanking == null) {
                // 첫 플레이 - 새 랭킹 등록
                insertRanking(contentType, score, userNo);
                log.info("신규 랭킹 등록 완료");
            } else {
                // 재플레이 - 기존 랭킹 업데이트
                updateRanking(existingRanking.getRankNo(), score);
                log.info("기존 랭킹 업데이트 완료");
            }
        } catch (Exception e) {
            log.error("랭킹 처리 중 오류 발생", e);
            throw new AppException("랭킹 처리에 실패했습니다.");
        }
    }

    /**
     * 사용자의 모든 랭킹 삭제
     * @param userNo 사용자 번호
     */
    @Transactional
    public void deleteRankingsByUser(int userNo) {
        log.info("deleteRankingsByUser 서비스 실행 - 사용자: {}", userNo);

        try {
            rankingMapper.deleteRankingsByUser(userNo);
            log.info("사용자 랭킹 삭제 완료");
        } catch (DataAccessException e) {
            log.error("사용자 랭킹 삭제 실패", e);
            throw new AppException("랭킹을 삭제하지 못했습니다.");
        }
    }

}