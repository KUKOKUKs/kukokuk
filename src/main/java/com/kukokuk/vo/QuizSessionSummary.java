package com.kukokuk.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("QuizSessionSummary")
public class QuizSessionSummary {
  private int sessionNo;
  private float totalAt;
  private int totalQuestion;
  private int correctAnswers;
  private int percentile;
  private float averageTimePerQuestion;
  private int userNo;
}
