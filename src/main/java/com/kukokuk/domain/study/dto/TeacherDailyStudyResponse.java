package com.kukokuk.domain.study.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherDailyStudyResponse {
    private int dailyStudyNo;            // 일일학습 ID
    private String dailyStudyTitle;         // 학습자료 제목
    private String explanation;         // 학습자료 제목
    private int difficulty;           // 학습 난이도 (1~6)
    private String materialTitle;        // 원본 파일명
    private String sourceFileUrl;         // Object Storage 경로
    private Integer completedStudentCount; // 학습 완료 학생 수
    private Integer essayCompletedStudentCount;   // 서술형퀴즈 완료 학생 수
    private Date createdDate;      // 생성일시
}
