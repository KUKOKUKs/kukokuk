package com.kukokuk.domain.study.dto;

import com.kukokuk.domain.study.vo.DailyStudy;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupParseMaterialResponse {
    private int dailyStudyMaterialNo;
    private String materialTitle; // 원본 파일명
    private String sourceFilename; // Object Storage 경로 (파일 다운로드용)
    private int difficulty; // 교사가 설정한 난이도
}