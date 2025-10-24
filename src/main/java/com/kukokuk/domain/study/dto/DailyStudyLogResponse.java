package com.kukokuk.domain.study.dto;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학습 이력 수정 요청의 응답으로 전달하는 response Dto
 */
@Getter
@Setter
@NoArgsConstructor
public class DailyStudyLogResponse {
    private int dailyStudyLogNo;
    private Integer studiedCardCount;
    private Date completedDate;
    private String status;
    private Date createdDate;
    private Date updatedDate;
    private int userNo;
    private int dailyStudyNo;
}
