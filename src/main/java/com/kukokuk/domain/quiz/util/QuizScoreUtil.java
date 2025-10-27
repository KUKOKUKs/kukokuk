package com.kukokuk.domain.quiz.util;

import java.math.BigDecimal;
import lombok.experimental.UtilityClass;

/**
 * Quiz 도메인 전용 점수 계산 유틸 클래스
 * 스피드, 단계별 퀴즈의 절대값 점수 계산 담당
 */
@UtilityClass
public class QuizScoreUtil {

    // 스피드 퀴즈 점수 계산 상수 (QuizProcessService와 동일)
    private static final float MAX_TIME = 300f; // 5분 (300초)
    private static final float MIN_TIME = 30f;   // 30초
    private static final int BASE_SCORE_MULTIPLIER = 100;
    private static final int TIME_BONUS_MULTIPLIER = 5;

    /**
     * 스피드 퀴즈 절대값 점수 계산
     * 기존 QuizProcessService.calculateSpeedQuizScore() 로직과 동일
     *
     * BASE_SCORE: (정답수 ÷ 전체문제수) × 100
     * TIME_BONUS: ((MAX_TIME - 실제시간) ÷ (MAX_TIME - MIN_TIME)) × 5
     * TOTAL_SCORE: BASE_SCORE + TIME_BONUS
     *
     * @param correctAnswers 정답 수
     * @param totalQuestion 전체 문제 수
     * @param totalTimeSec 총 시간 (초)
     * @return 계산된 절대값 점수
     */
    public static BigDecimal calculateSpeedScore(int correctAnswers, int totalQuestion, float totalTimeSec) {
        if (totalQuestion <= 0) {
            return BigDecimal.ZERO;
        }

        // BASE_SCORE: (정답수 ÷ 전체문제수) × 100
        double baseScore = ((double) correctAnswers / totalQuestion) * BASE_SCORE_MULTIPLIER;

        // TIME_BONUS: ((MAX_TIME - 실제시간) ÷ (MAX_TIME - MIN_TIME)) × 5
        float actualTime = Math.max(MIN_TIME, Math.min(MAX_TIME, totalTimeSec));
        double timeBonus = ((MAX_TIME - actualTime) / (MAX_TIME - MIN_TIME)) * TIME_BONUS_MULTIPLIER;

        // TOTAL_SCORE: BASE_SCORE + TIME_BONUS
        double finalScore = baseScore + timeBonus;

        return BigDecimal.valueOf(finalScore);
    }

    /**
     * 단계별 퀴즈 절대값 점수 계산
     * 정답률 기반: (정답수 ÷ 전체문제수) × 100
     *
     * @param correctAnswers 정답 수
     * @param totalQuestion 전체 문제 수
     * @return 계산된 절대값 점수
     */
    public static BigDecimal calculateLevelScore(int correctAnswers, int totalQuestion) {
        if (totalQuestion <= 0) {
            return BigDecimal.ZERO;
        }

        // 정답률 기반 점수: (정답수 ÷ 전체문제수) × 100
        double score = ((double) correctAnswers / totalQuestion) * BASE_SCORE_MULTIPLIER;

        return BigDecimal.valueOf(score);
    }
}