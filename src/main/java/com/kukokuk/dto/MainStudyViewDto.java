package com.kukokuk.dto;

import com.kukokuk.vo.DailyQuest;
import com.kukokuk.vo.DailyQuestUser;
import com.kukokuk.vo.DailyStudy;
import com.kukokuk.vo.DailyStudyLog;
import com.kukokuk.vo.User;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MainStudyViewDto {

  // 학습 히스토리
  private List<DailyStudyLog> dailyStudyLogs;

  // 유저
  private User user;
  // 학습의 일일 도전과제
  private List<DailyQuest> dailyQuests;

  // 사용자가 일일 도전과제 성공 관련 정보
  private List<DailyQuestUser> dailyQuestUsers;

  // 일일학습 목록
  private List<DailyStudy> dailyStudies;

}
