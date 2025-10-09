package com.kukokuk.domain.study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 맞춤 학습자료 목록 조회 요청의 응답으로 전달하는 response Dto
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStudySummaryResponse {

    private int dailyStudyNo;           // 일일학습 고유 번호
    private String title;               // 학습자료 제목
    private String explanation;         // 학습자료 설명
    private int cardCount;              // 전체 카드 개수
    private String status;              // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    private int studiedCardCount;       // 사용자가 학습한 카드 개수
    private int progressRate;           // 학습 진행률 (0~100)
    private String school;              // "초등" 또는 "중등"
    private Integer grade;                  // 학년
    private int sequence;               // 학년 내 자료 순서
    private boolean essayQuizCompleted; // 사용자의 서술형 퀴즈 완료 여부
}
