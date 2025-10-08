package com.kukokuk.domain.rank.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 랭킹 위젯용 데이터 전송 객체
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class RankingWidgetDto {

    private List<RanksResponseDto> speedRankings;
    private List<RanksResponseDto> stepRankings;
    private List<RanksResponseDto> dictationRankings;

    private Integer mySpeedRank;
    private Integer myStepRank;
    private Integer myDictationRank;
}