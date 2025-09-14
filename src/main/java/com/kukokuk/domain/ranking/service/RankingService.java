package com.kukokuk.domain.ranking.service;

import com.kukokuk.domain.ranking.mapper.RankingMapper;
import com.kukokuk.domain.ranking.vo.Ranking;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 월별 랭킹 관련 서비스
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingMapper rankingMapper;

    /**
     * 현재 년월(YYYY-MM) 반환
     */
    private String getCurrentMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /**
     * 새로운 월별 랭킹 등록 (첫 플레이)
     * @param contentType 컨텐츠 타입
     * @param score 계산된 점수
     * @param userNo 사용자 번호
     */
    @Transactional
    public void insertMonthlyRanking(String contentType, double score, int userNo) {
        String currentMonth = getCurrentMonth();
        log.info("insertMonthlyRanking 서비스 실행 - 사용자: {}, 컨텐츠: {}, 점수: {}, 월: {}",
            userNo, contentType, score, currentMonth);

        Ranking ranking = new Ranking();
        ranking.setContentType(contentType);
        ranking.setPlayCount(1);
        ranking.setTotalScore(BigDecimal.valueOf(score));
        ranking.setRankMonth(currentMonth);
        ranking.setUserNo(userNo);

        rankingMapper.insertRanking(ranking);
        log.info("신규 월별 랭킹 등록 완료 - 랭킹번호: {}, 월: {}", ranking.getRankNo(), currentMonth);
    }

    /**
     * 기존 월별 랭킹 업데이트 (재플레이 시 누적 평균 계산)
     * @param rankNo 기존 랭킹 번호
     * @param newScore 새로운 점수
     */
    @Transactional
    public void updateMonthlyRanking(int rankNo, double newScore) {
        log.info("updateMonthlyRanking 서비스 실행 - 랭킹번호: {}, 새점수: {}", rankNo, newScore);

        Ranking existingRanking = rankingMapper.getRankingByRankNo(rankNo);
        if (existingRanking == null) {
            log.error("랭킹 정보를 찾을 수 없음: {}", rankNo);
            throw new IllegalArgumentException("랭킹 정보를 찾을 수 없습니다.");
        }

        // 누적 평균 계산
        int currentPlayCount = existingRanking.getPlayCount();
        double currentTotalScore = existingRanking.getTotalScore().doubleValue();

        // 새로운 누적 절대값 = (현재 평균 점수 × 현재 플레이 횟수) + 새 점수
        double newCumulativeScore = (currentTotalScore * currentPlayCount) + newScore;
        int newPlayCount = currentPlayCount + 1;

        // 새로운 평균 점수 = 누적 절대값 / 새로운 플레이 횟수
        double newAverageScore = newCumulativeScore / newPlayCount;

        log.info("월별 평균 점수 계산: {}회차 {:.2f} + 새점수 {:.2f} = 평균 {:.2f} ({}월)",
            currentPlayCount, currentTotalScore, newScore, newAverageScore,
            existingRanking.getRankMonth());

        // 랭킹 업데이트
        existingRanking.setPlayCount(newPlayCount);
        existingRanking.setTotalScore(BigDecimal.valueOf(newAverageScore));

        rankingMapper.updateRanking(existingRanking);
        log.info("월별 랭킹 업데이트 완료");
    }

    /**
     * 사용자의 특정 컨텐츠 현재 월 랭킹 조회
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @return 랭킹 정보, 없으면 null
     */
    public Ranking getRankingByUserAndContent(int userNo, String contentType) {
        log.info("getRankingByUserAndContent 서비스 실행 - 사용자: {}, 컨텐츠: {}, 현재월: {}",
            userNo, contentType, getCurrentMonth());
        return rankingMapper.getRankingByUserAndContent(userNo, contentType);
    }

    /**
     * 사용자의 특정 컨텐츠 특정 월 랭킹 조회
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @param rankMonth 조회할 월 (YYYY-MM 형태)
     * @return 랭킹 정보, 없으면 null
     */
    public Ranking getRankingByUserContentAndMonth(int userNo, String contentType, String rankMonth) {
        log.info("getRankingByUserContentAndMonth 서비스 실행 - 사용자: {}, 컨텐츠: {}, 월: {}",
            userNo, contentType, rankMonth);
        return rankingMapper.getRankingByUserContentAndMonth(userNo, contentType, rankMonth);
    }

    /**
     * 전체 월별 랭킹 목록 조회 (현재 월 기준)
     * @param contentType 컨텐츠 타입
     * @param limit 조회할 개수
     * @return 전체 랭킹 목록 (점수 높은 순)
     */
    public List<Ranking> getGlobalRankings(String contentType, int limit) {
        log.info("getGlobalRankings 서비스 실행 - 컨텐츠: {}, 개수: {}, 현재월: {}",
            contentType, limit, getCurrentMonth());
        return rankingMapper.getGlobalRankings(contentType, limit);
    }

    /**
     * 전체 월별 랭킹 목록 조회 (특정 월 기준)
     * @param contentType 컨텐츠 타입
     * @param rankMonth 조회할 월 (YYYY-MM 형태)
     * @param limit 조회할 개수
     * @return 전체 랭킹 목록 (점수 높은 순)
     */
    public List<Ranking> getGlobalRankingsByMonth(String contentType, String rankMonth, int limit) {
        log.info("getGlobalRankingsByMonth 서비스 실행 - 컨텐츠: {}, 월: {}, 개수: {}",
            contentType, rankMonth, limit);
        return rankingMapper.getGlobalRankingsByMonth(contentType, rankMonth, limit);
    }

    /**
     * 그룹 내 월별 랭킹 목록 조회 (현재 월 기준)
     * @param contentType 컨텐츠 타입
     * @param groupNo 그룹 번호
     * @param limit 조회할 개수
     * @return 그룹 내 랭킹 목록 (점수 높은 순)
     */
    public List<Ranking> getGroupRankings(String contentType, int groupNo, int limit) {
        log.info("getGroupRankings 서비스 실행 - 컨텐츠: {}, 그룹: {}, 개수: {}, 현재월: {}",
            contentType, groupNo, limit, getCurrentMonth());
        return rankingMapper.getGroupRankings(contentType, groupNo, limit);
    }

    /**
     * 그룹 내 월별 랭킹 목록 조회 (특정 월 기준)
     * @param contentType 컨텐츠 타입
     * @param groupNo 그룹 번호
     * @param rankMonth 조회할 월 (YYYY-MM 형태)
     * @param limit 조회할 개수
     * @return 그룹 내 랭킹 목록 (점수 높은 순)
     */
    public List<Ranking> getGroupRankingsByMonth(String contentType, int groupNo, String rankMonth, int limit) {
        log.info("getGroupRankingsByMonth 서비스 실행 - 컨텐츠: {}, 그룹: {}, 월: {}, 개수: {}",
            contentType, groupNo, rankMonth, limit);
        return rankingMapper.getGroupRankingsByMonth(contentType, groupNo, rankMonth, limit);
    }

    /**
     * 전체 랭킹에서 사용자 순위 조회 (현재 월 기준)
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @return 전체 순위 (1부터 시작)
     */
    public int getGlobalUserRanking(int userNo, String contentType) {
        log.info("getGlobalUserRanking 서비스 실행 - 사용자: {}, 컨텐츠: {}, 현재월: {}",
            userNo, contentType, getCurrentMonth());
        return rankingMapper.getGlobalUserRanking(userNo, contentType);
    }

    /**
     * 전체 랭킹에서 사용자 순위 조회 (특정 월 기준)
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @param rankMonth 조회할 월 (YYYY-MM 형태)
     * @return 전체 순위 (1부터 시작)
     */
    public int getGlobalUserRankingByMonth(int userNo, String contentType, String rankMonth) {
        log.info("getGlobalUserRankingByMonth 서비스 실행 - 사용자: {}, 컨텐츠: {}, 월: {}",
            userNo, contentType, rankMonth);
        return rankingMapper.getGlobalUserRankingByMonth(userNo, contentType, rankMonth);
    }

    /**
     * 그룹 내 랭킹에서 사용자 순위 조회 (현재 월 기준)
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @param groupNo 그룹 번호
     * @return 그룹 내 순위 (1부터 시작)
     */
    public int getGroupUserRanking(int userNo, String contentType, int groupNo) {
        log.info("getGroupUserRanking 서비스 실행 - 사용자: {}, 컨텐츠: {}, 그룹: {}, 현재월: {}",
            userNo, contentType, groupNo, getCurrentMonth());
        return rankingMapper.getGroupUserRanking(userNo, contentType, groupNo);
    }

    /**
     * 그룹 내 랭킹에서 사용자 순위 조회 (특정 월 기준)
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @param groupNo 그룹 번호
     * @param rankMonth 조회할 월 (YYYY-MM 형태)
     * @return 그룹 내 순위 (1부터 시작)
     */
    public int getGroupUserRankingByMonth(int userNo, String contentType, int groupNo, String rankMonth) {
        log.info("getGroupUserRankingByMonth 서비스 실행 - 사용자: {}, 컨텐츠: {}, 그룹: {}, 월: {}",
            userNo, contentType, groupNo, rankMonth);
        return rankingMapper.getGroupUserRankingByMonth(userNo, contentType, groupNo, rankMonth);
    }

    /**
     * 사용자의 모든 랭킹 삭제
     * @param userNo 사용자 번호
     */
    @Transactional
    public void deleteRankingsByUser(int userNo) {
        log.info("deleteRankingsByUser 서비스 실행 - 사용자: {}", userNo);
        rankingMapper.deleteRankingsByUser(userNo);
        log.info("사용자 랭킹 삭제 완료");
    }

    /**
     * 사용자의 월별 랭킹 히스토리 조회
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 타입
     * @param limit 조회할 개수 (최근 월부터)
     * @return 월별 랭킹 히스토리 목록
     */
    public List<Ranking> getUserMonthlyHistory(int userNo, String contentType, int limit) {
        log.info("getUserMonthlyHistory 서비스 실행 - 사용자: {}, 컨텐츠: {}, 개수: {}",
            userNo, contentType, limit);
        return rankingMapper.getUserMonthlyHistory(userNo, contentType, limit);
    }

    /**
     * 월별 랭킹 처리 (기존 랭킹이 있으면 업데이트, 없으면 신규 등록)
     * 현재 월 기준으로 처리
     * @param contentType 컨텐츠 타입
     * @param score 점수
     * @param userNo 사용자 번호
     */
    @Transactional
    public void processMonthlyRanking(String contentType, double score, int userNo) {
        String currentMonth = getCurrentMonth();
        log.info("processMonthlyRanking 서비스 실행 - 사용자: {}, 컨텐츠: {}, 점수: {}, 현재월: {}",
            userNo, contentType, score, currentMonth);

        // 현재 월의 기존 랭킹 조회
        Ranking existingRanking = getRankingByUserAndContent(userNo, contentType);

        if (existingRanking == null) {
            // 해당 월 첫 플레이 - 새 랭킹 등록
            insertMonthlyRanking(contentType, score, userNo);
            log.info("현재월({}) 신규 랭킹 등록 완료", currentMonth);
        } else {
            // 같은 월 재플레이 - 기존 랭킹 업데이트
            updateMonthlyRanking(existingRanking.getRankNo(), score);
            log.info("현재월({}) 기존 랭킹 업데이트 완료", currentMonth);
        }
    }

    /**
     * 특정 월 랭킹 처리 (기존 랭킹이 있으면 업데이트, 없으면 신규 등록)
     * @param contentType 컨텐츠 타입
     * @param score 점수
     * @param userNo 사용자 번호
     * @param rankMonth 처리할 월 (YYYY-MM 형태)
     */
    @Transactional
    public void processMonthlyRankingByMonth(String contentType, double score, int userNo, String rankMonth) {
        log.info("processMonthlyRankingByMonth 서비스 실행 - 사용자: {}, 컨텐츠: {}, 점수: {}, 월: {}",
            userNo, contentType, score, rankMonth);

        // 특정 월의 기존 랭킹 조회
        Ranking existingRanking = getRankingByUserContentAndMonth(userNo, contentType, rankMonth);

        if (existingRanking == null) {
            // 해당 월 첫 플레이 - 새 랭킹 등록
            Ranking ranking = new Ranking();
            ranking.setContentType(contentType);
            ranking.setPlayCount(1);
            ranking.setTotalScore(BigDecimal.valueOf(score));
            ranking.setRankMonth(rankMonth);
            ranking.setUserNo(userNo);

            rankingMapper.insertRanking(ranking);
            log.info("특정월({}) 신규 랭킹 등록 완료", rankMonth);
        } else {
            // 같은 월 재플레이 - 기존 랭킹 업데이트
            updateMonthlyRanking(existingRanking.getRankNo(), score);
            log.info("특정월({}) 기존 랭킹 업데이트 완료", rankMonth);
        }
    }

    // 기존 호환성을 위한 deprecated 메서드들 (점진적 마이그레이션용)

    /**
     * @deprecated 월별 랭킹으로 변경됨. processMonthlyRanking() 사용 권장
     */
    @Deprecated
    @Transactional
    public void processRanking(String contentType, double score, int userNo) {
        log.warn("Deprecated processRanking() 호출됨. processMonthlyRanking() 사용 권장");
        processMonthlyRanking(contentType, score, userNo);
    }

    /**
     * @deprecated 월별 랭킹으로 변경됨. insertMonthlyRanking() 사용 권장
     */
    @Deprecated
    @Transactional
    public void insertRanking(String contentType, double score, int userNo) {
        log.warn("Deprecated insertRanking() 호출됨. insertMonthlyRanking() 사용 권장");
        insertMonthlyRanking(contentType, score, userNo);
    }

    /**
     * @deprecated 월별 랭킹으로 변경됨. updateMonthlyRanking() 사용 권장
     */
    @Deprecated
    @Transactional
    public void updateRanking(int rankNo, double newScore) {
        log.warn("Deprecated updateRanking() 호출됨. updateMonthlyRanking() 사용 권장");
        updateMonthlyRanking(rankNo, newScore);
    }
}