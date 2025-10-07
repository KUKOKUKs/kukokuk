package com.kukokuk.domain.ranking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 랭킹 위젯용 데이터 전송 객체
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class RankingWidgetDto {

    private List<RankingDto> speedRankings;
    private List<RankingDto> stepRankings;
    private List<RankingDto> dictationRankings;

    private Integer mySpeedRank;
    private Integer myStepRank;
    private Integer myDictationRank;
}