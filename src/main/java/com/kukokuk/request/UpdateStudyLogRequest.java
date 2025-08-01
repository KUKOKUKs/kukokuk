package com.kukokuk.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudyLogRequest {
  private Integer studiedCardCount; // Null 허용
  private String status;  // "IN_PROGRESS" or "COMPLETED"
}
