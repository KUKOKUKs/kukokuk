package com.kukokuk.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("QuizMaster")
public class QuizMaster {
  private int quizNo;
  private int entryNo;
  private String question;
  private String option1;
  private String option2;
  private String option3;
  private String option4;
  private int successAnswer;
  private String questionType;   // ENUM('단어','뜻')
  private String difficulty;     // ENUM('상','중','하')
  private int usageCount;
  private int successCount;
}
