package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyStudyQuizLog")
public class DailyStudyQuizLog {
  private int dailyStudyQuizLogNo;
  private String isSuccess; // ENUM("Y", "N")
  private int selectedChoice;
  private Date createdDate;
  private Date updatedDate;
  private int userNo;
  private int dailyStudyQuizNo;

  private DailyStudyQuiz dailyStudyQuiz;
}