package com.kukokuk.domain.study.dto;

import com.kukokuk.domain.study.vo.DailyStudyLog;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MainStudyViewDto {

  // 학습 히스토리
  private List<DailyStudyLog> dailyStudyLogs;

  // 학습의 일일 도전과제와 사용자의 도전과제 이력 DTO
  private List<DailyQuestDto> dailyQuestDtos;

}
