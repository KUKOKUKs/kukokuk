package com.kukokuk.domain.ranking.service;

import com.kukokuk.domain.quiz.vo.QuizSessionSummary;
import com.kukokuk.domain.ranking.mapper.RankingMapper;
import com.kukokuk.domain.ranking.mapper.ScoreCalculationMapper;
import com.kukokuk.domain.ranking.vo.Ranking;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingMapper rankingMapper;
    private final ScoreCalculationMapper scoreCalculationMapper;

    /**
     * 전체 사용자 랭킹 조회
     *
     * @param contentType 컨텐츠 유형 (SPEED, LEVEL, DICTATION 등)
     * @return 랭킹 리스트
     */
    @Transactional(readOnly = true)
    public List<Ranking> getAllRankings(String contentType) {
        log.info("전체 랭킹 조회 - contentType: {}", contentType);
        return rankingMapper.getAllRankingsByContentType(contentType);
    }

    /**
     * 반별(그룹) 랭킹 조회
     *
     * @param contentType 컨텐츠 유형
     * @param groupNo 그룹 번호
     * @return 그룹 내 랭킹 리스트
     */
    @Transactional(readOnly = true)
    public List<Ranking> getGroupRankings(String contentType, int groupNo) {
        log.info("그룹 랭킹 조회 - contentType: {}, groupNo: {}", contentType, groupNo);
        return rankingMapper.getGroupRankingsByContentType(contentType, groupNo);
    }

    /**
     * 특정 사용자의 랭킹 정보 조회
     *
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 유형
     * @return 사용자 랭킹 정보
     */
    @Transactional(readOnly = true)
    public Ranking getUserRanking(int userNo, String contentType) {
        return rankingMapper.getUserRanking(userNo, contentType);
    }

    /**
     * 스피드 퀴즈 세션 종료 시 랭킹 업데이트
     * BASE_SCORE + TIME_BONUS 계산하여 TOTAL_SCORE 산출
     *
     * @param sessionNo 세션 번호
     */
    @Transactional
    public void updateSpeedQuizRanking(int sessionNo) {
        log.info("스피드 퀴즈 랭킹 업데이트 시작 - sessionNo: {}", sessionNo);

        QuizSessionSummary session = scoreCalculationMapper.getQuizSessionSummary(sessionNo);
        if (session == null) {
            log.error("세션 정보 없음 - sessionNo: {}", sessionNo);
            throw new IllegalArgumentException("존재하지 않는 세션입니다.");
        }

        // BASE_SCORE 계산: (정답수 / 전체문제수) * 100
        double baseScore = ((double) session.getCorrectAnswers() / session.getTotalQuestion()) * 100;

        // TIME_BONUS 계산: 동일 정답수 그룹 내에서만 시간 보너스
        double timeBonus = calculateTimeBonus(session.getCorrectAnswers(), session.getTotalTimeSec());

        // TOTAL_SCORE = BASE_SCORE + TIME_BONUS
        double totalScore = baseScore + timeBonus;

        log.info("점수 계산 완료 - userNo: {}, baseScore: {}, timeBonus: {}, totalScore: {}",
            session.getUserNo(), baseScore, timeBonus, totalScore);

        // 랭킹 정보 업데이트 또는 신규 등록
        updateOrInsertRanking(session.getUserNo(), "SPEED", totalScore);
    }

    /**
     * 단계별 퀴즈 세션 종료 시 랭킹 업데이트
     * 단계별 퀴즈는 시간 보너스 없이 BASE_SCORE만 적용
     *
     * @param sessionNo 세션 번호
     */
    @Transactional
    public void updateLevelQuizRanking(int sessionNo) {
        log.info("단계별 퀴즈 랭킹 업데이트 시작 - sessionNo: {}", sessionNo);

        // ScoreCalculationMapper 사용
        QuizSessionSummary session = scoreCalculationMapper.getQuizSessionSummary(sessionNo);
        if (session == null) {
            log.error("세션 정보 없음 - sessionNo: {}", sessionNo);
            throw new IllegalArgumentException("존재하지 않는 세션입니다.");
        }

        // 단계별 퀴즈는 정답률만 적용 (시간 보너스 없음)
        double totalScore = ((double) session.getCorrectAnswers() / session.getTotalQuestion()) * 100;

        log.info("단계별 퀴즈 점수 계산 완료 - userNo: {}, totalScore: {}",
            session.getUserNo(), totalScore);

        updateOrInsertRanking(session.getUserNo(), "LEVEL", totalScore);
    }

    /**
     * 동일 정답수 그룹 내에서 시간 보너스 계산
     * TIME_BONUS = ((MAX_TIME - 실제시간) ÷ (MAX_TIME - MIN_TIME)) × 5
     *
     * @param correctAnswers 정답 수
     * @param actualTime 실제 소요 시간
     * @return 시간 보너스 (0~5점)
     */
    private double calculateTimeBonus(int correctAnswers, float actualTime) {
        // 동일 정답수 그룹의 시간 범위 조회
        Float maxTime = scoreCalculationMapper.getMaxTimeByCorrectAnswers(correctAnswers);
        Float minTime = scoreCalculationMapper.getMinTimeByCorrectAnswers(correctAnswers);

        if (maxTime == null || minTime == null || maxTime.equals(minTime)) {
            log.info("시간 보너스 계산 불가 - correctAnswers: {}, maxTime: {}, minTime: {}",
                correctAnswers, maxTime, minTime);
            return 0.0; // 비교 데이터 없으면 보너스 0점
        }

        // TIME_BONUS 계산
        double timeBonus = ((maxTime - actualTime) / (maxTime - minTime)) * 5.0;
        timeBonus = Math.max(0.0, Math.min(5.0, timeBonus)); // 0~5점 범위 제한

        log.info("시간 보너스 계산 - actualTime: {}, maxTime: {}, minTime: {}, bonus: {}",
            actualTime, maxTime, minTime, timeBonus);

        return timeBonus;
    }

    /**
     * 랭킹 정보 업데이트 또는 신규 등록
     * 기존 랭킹이 있으면 업데이트, 없으면 신규 등록
     *
     * @param userNo 사용자 번호
     * @param contentType 컨텐츠 유형
     * @param totalScore 총 점수
     */
    private void updateOrInsertRanking(int userNo, String contentType, double totalScore) {
        int existingCount = rankingMapper.getRankingCountByUserAndContent(userNo, contentType);

        if (existingCount > 0) {
            // 기존 랭킹 업데이트
            Ranking existing = rankingMapper.getUserRanking(userNo, contentType);
            existing.setPlayCount(existing.getPlayCount() + 1);
            existing.setTotalScore(totalScore);

            rankingMapper.updateRanking(existing);
            log.info("랭킹 업데이트 완료 - userNo: {}, contentType: {}, playCount: {}, totalScore: {}",
                userNo, contentType, existing.getPlayCount(), totalScore);
        } else {
            // 신규 랭킹 등록
            Ranking newRanking = new Ranking();
            newRanking.setUserNo(userNo);
            newRanking.setContentType(contentType);
            newRanking.setPlayCount(1);
            newRanking.setTotalScore(totalScore);

            rankingMapper.insertRanking(newRanking);
            log.info("신규 랭킹 등록 완료 - userNo: {}, contentType: {}, totalScore: {}",
                userNo, contentType, totalScore);
        }
    }

    /**
     * 사용자 랭킹 삭제 (사용자 탈퇴 시 등)
     *
     * @param userNo 사용자 번호
     */
    @Transactional
    public void deleteUserRanking(int userNo) {
        rankingMapper.deleteRankingByUserNo(userNo);
        log.info("사용자 랭킹 삭제 완료 - userNo: {}", userNo);
    }
}