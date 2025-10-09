package com.kukokuk.integration.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class WorkerMaterialCallbackRequest {
    private String jobId;            // Redis JobStatus와 연결되는 키
    private int dailyStudyMaterialNo;
    private Integer difficulty;
    private String content;
    private int groupNo;
}
