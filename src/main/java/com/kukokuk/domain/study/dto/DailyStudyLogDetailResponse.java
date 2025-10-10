package com.kukokuk.domain.study.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("DailyStudyLogDetailResponse")
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
    private Integer totalQuizCount;         // 객관식 퀴즈 총 개수
    private Integer successedQuizCount;         // 사용자가 맞힌 퀴즈 개수
    private boolean essaySubmitted;       // 서술형 퀴즈 제출 여부
    private int difficulty;            // 학습 난이도 (1~6)
}
