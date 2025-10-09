package com.kukokuk.domain.study.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStudyLogDetailResponse {
    private int dailyStudyLogNo;          // 학습 이력 번호
    private int dailyStudyNo;             // 일일 학습 번호
    private String dailyStudyTitle;       // 일일 학습 제목
    private String status;                // 학습 상태 (IN_PROGRESS / COMPLETED)
    private Date startedDate;    // 학습 시작일
    private Date updatedDate;    // 마지막 업데이트일
    private int progressRate;             // 학습 진행률 (%)
    private int totalCardCount;           // 전체 학습 카드 수
    private int completedCardCount;       // 사용자가 완료한 카드 수
    private Integer quizAccuracy;         // 객관식 퀴즈 정답률
    private boolean essaySubmitted;       // 서술형 퀴즈 제출 여부
    private int expGained;                // 획득 경험치
    private String difficulty;            // 학습 난이도 (상/중/하)
}
