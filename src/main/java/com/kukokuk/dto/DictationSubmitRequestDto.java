package com.kukokuk.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictationSubmitRequestDto {
  private int userNo;
  private List<DictationAnswerDto> answers;
}
