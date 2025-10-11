package com.kukokuk.domain.study.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdminParseMaterialResponse {
    private int dailyStudyMaterialNo; // DB에 저장된 학습자료 PK
    private String materialTitle;     // 원본 파일명 (ex: "3학년_사회_1단원.hwpx")
    private String sourceFilename;    // Object Storage 경로 (다운로드용)
    private String school;            // 학교명
    private int grade;                // 학년
}