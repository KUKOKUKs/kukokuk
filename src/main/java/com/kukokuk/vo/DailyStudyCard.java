package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyStudyCard")
public class DailyStudyCard {
  private int dailyStudyCardNo;
  private String title;
  private String content;
  private Date createdDate;
  private Date updatedDate;
  private int cardIndex;
  private int dailyStudyNo;
}
