package com.kukokuk.response;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private boolean hintObtained; // 힌트를 획득했는지
}
