package com.kukokuk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictationAnswerDto {
  private int userNo;
  private int dictationSessionNo;
  private int dictationQuestionNo;
  private String userAnswer;
  private String usedHint;

}
