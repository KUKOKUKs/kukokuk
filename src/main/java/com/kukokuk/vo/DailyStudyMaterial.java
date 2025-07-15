package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyStudyMaterial")
public class DailyStudyMaterial {
  private int dailyStudyMaterialNo;
  private String school;
  private int grade;
  private String materialTitle;
  private String keywords;
  private String content;
  private String sourceFilename;
  private int sequence;
  private Date createdDate;
  private Date updatedDate;
}