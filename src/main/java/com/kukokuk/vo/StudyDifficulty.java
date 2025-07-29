package com.kukokuk.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@Alias("StudyDifficulty")
public class StudyDifficulty {
  private int studyDifficultyNo;
  private String explanation;
  private String promptText;
}
