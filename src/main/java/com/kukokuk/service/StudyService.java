package com.kukokuk.service;

import com.kukokuk.dto.MainStudyViewDto;
import com.kukokuk.mapper.DailyQuestMapper;
import com.kukokuk.mapper.DailyStudyMapper;
import com.kukokuk.mapper.DailyStudyMapper;
import com.kukokuk.vo.DailyQuest;
import com.kukokuk.vo.DailyQuestUser;
import com.kukokuk.vo.DailyStudy;
import com.kukokuk.vo.DailyStudyLog;
import com.kukokuk.vo.User;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class StudyService {
  @Autowired
  private DailyStudyMapper dailyStudyMapper;

  @Autowired
  private DailyQuestMapper dailyQuestMapper;

  /*
    메인 화면에 필요한 데이터를 반환한다
    MainStudyViewDto 에 포함되는 데이터
      - 유저 정보
      - 유저의 수준에 맞는 일일학습 목록
      - 사용자의 이전 학습 이력 목록
      - 학습탭의 일일 도전과제
      - 사용자_일일 도전과제 관련 정보 (아이템 획득 여부)
   */
  public MainStudyViewDto getMainStudyView(UserDetails userDetails) {
    MainStudyViewDto dto = new MainStudyViewDto();

    // 학습탭의 일일 도전과제 정보 조회 (인증된 유저와 관련 X)
    List<DailyQuest> dailyQuests = dailyQuestMapper.getDailyQuestByContentType("STUDY");
    dto.setDailyQuests(dailyQuests);


    // 인증된 사용자일때
    if(userDetails != null){

      /*
        테스트를 위한 유저 객체 생성
        ------------------------------
       */
      User user = new User();
      user.setUserNo(1);
      user.setStudyDifficulty(4);
      /*
        ------------------------------
       */

      // User user = userDetails.getUser();
      // dto에 사용자 정보 설정
      dto.setUser(user);

      // 유저의 수준에 맞고, 유저가 아직 학습하지 않았거나 학습중인 일일학습 5개 조회
      /*
      고려할 사항
        1. 유저의 수준 (STUDY_DIFFICULTY)
        2. 유저의 일일학습 이력에서 학습완료 되지 않은 학습자료만 불러옴
        3. 일일학습자료의 조건 : 일일학습의 원본데이터의 학년에 맞는 자료를 자료순서에 불러옴
        4. 몇개의 행을 가져올지 조회조건 전달
        예를들어 유저의 수준이 4(중1~중2)면, 학년이 중1~중2인 원본데이터를 참조하는 학습자료중에서,
        학습수준이 4인 학습자료의 목록을 조회하기 => 원본데이터의 자료순서로 정렬!
        만약 중1, 중2 학습자
      */
      Map<String, Object> dailyStudyCondition = new HashMap<>();

      List<DailyStudy> dailyStudies = dailyStudyMapper.getDailyStudiesByUser(
          user.getUserNo(),
          user.getStudyDifficulty(),
          dailyStudyCondition
      );
      dto.setDailyStudies(dailyStudies);

      // 사용자 일일학습 자료가 5개 이하면, 학습자료 새로 생성하기


      // 사용자의 이전 학습이력 목록 5개 조회
      /*
      고려할 사항
        1. updatedDate로 정렬
        2. 조회 조건 전달 (개수)
      */
      Map<String, Object> dailyStudyLogCondition = new HashMap<>();
      dailyStudyLogCondition.put("rows", 5);
      dailyStudyLogCondition.put("order", "updatedDate");
      List<DailyStudyLog> dailyStudyLogs = dailyStudyMapper.getDailyStudyLogsByUserNo(user.getUserNo(), dailyStudyLogCondition);
      dto.setDailyStudyLogs(dailyStudyLogs);

      // 사용자의 일일 도전과제 정보 조회
      /*
       고려할 사항
        1. 오늘 날짜와 컨텐츠타입 전달
       */
      Map<String, Object> dailyQuestUserCondition = new HashMap<>();
      dailyQuestUserCondition.put("completedDate", new Date());
      dailyQuestUserCondition.put("contentType", "STUDY");
      List<DailyQuestUser> dailyQuestUsers = dailyQuestMapper.getDailyQuestUserByUserNo(user.getUserNo(), dailyQuestUserCondition);
      dto.setDailyQuestUsers(dailyQuestUsers);
    }

    return dto;
  }
}
