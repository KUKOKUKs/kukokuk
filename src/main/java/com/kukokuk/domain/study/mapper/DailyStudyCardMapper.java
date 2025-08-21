package com.kukokuk.domain.study.mapper;

import com.kukokuk.domain.study.vo.DailyStudyCard;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyStudyCardMapper {

    // 학습자료 카드를 생성
    void insertDailyStudyCard(DailyStudyCard dailyStudyCard);

    /**
     * 학습자료 번호로 해당 학습자료에 속한 카드 목록 조회
     *
     * @param dailyStudyNo 학습자료번호
     * @return 학습자료에 속한 카드 목록
     */
    List<DailyStudyCard> getCardsByDailyStudyNo(int dailyStudyNo);
}
