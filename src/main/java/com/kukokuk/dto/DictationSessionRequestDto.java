package com.kukokuk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictationSessionRequestDto {
  private int dictationSessionNo; // 받아쓰기 세트 번호
  private int userNo;             // 사용자 번호
}
