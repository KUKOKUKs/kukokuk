package com.kukokuk.domain.study.dto;

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
public class StudyMaterialJobPayload {
    private String jobId; // jobStatus 식별자
    private String fileUrl; // Object Storage 파일 경로
    private int groupId; // 그룹 ID
    private int difficulty; // 학습 난이도
    private int dailyStudyMaterialNo; // DB PK
}
