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
public class DailyStudyJobPayload {
    private String jobId;
    private int dailyStudyMaterialNo;
    private int studyDifficultyNo;
}
