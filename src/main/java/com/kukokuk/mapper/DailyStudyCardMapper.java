package com.kukokuk.mapper;

import com.kukokuk.vo.DailyStudyCard;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyStudyCardMapper {

  // 학습자료 카드를 생성
  void insertDailyStudyCard(DailyStudyCard dailyStudyCard);
}
