package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyStudyLog")
public class DailyStudyLog {
  private int dailyStudyLogNo;
  private int studiedCardCount;
  private Date completedDate;
  private String status;
  private Date createdDate;
  private Date updatedDate;
  private int userNo;
  private int dailyStudyNo;

  private DailyStudy dailyStudy;
}