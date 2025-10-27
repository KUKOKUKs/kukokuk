package com.kukokuk.domain.quiz.vo;

import com.kukokuk.domain.quiz.util.QuizScoreUtil;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("QuizSessionSummary")
public class QuizSessionSummary {

    private int sessionNo;                  // 세션 고유 번호 (PK)
    private int userNo;                     // 사용자 번호 (FK)

    private String quizMode;                // 퀴즈 유형: speed 또는 level
    private float totalTimeSec;             // 총 문제 풀이 시간
    private int totalQuestion;              // 전체 문항 수
    private int correctAnswers;             // 정답 수
    private int percentile;                 // 상위 퍼센티지
    private float averageTimePerQuestion;   // 문항당 평균 소요시간

    private String difficulty;              // 단계별일 경우 포함

    private Date createdDate;
    private Date updatedDate;

    public boolean isSpeedMode() {
        return "speed".equals(quizMode);
    }

    /**
     * 이력 조회 시 사용할 절대값 점수 계산 게터
     * 스피드: 복잡한 점수 계산 (기본점수 + 시간보너스)
     * 단계별: 정답률 기반 점수 (정답수/전체문제수 * 100)
     *
     * @return 계산된 절대값 점수
     */
    public BigDecimal getAbsoluteScore() {
        if (isSpeedMode()) {
            return QuizScoreUtil.calculateSpeedScore(correctAnswers, totalQuestion, totalTimeSec);
        } else {
            return QuizScoreUtil.calculateLevelScore(correctAnswers, totalQuestion);
        }
    }
}
