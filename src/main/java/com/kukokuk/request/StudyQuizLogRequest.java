package com.kukokuk.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학습퀴즈 이력 생성 및 수정에서 사용하는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class StudyQuizLogRequest {
  private Integer dailyStudyQuizNo; // null 허용 (수정시 필요X)
  private int selectedChoice;
}
