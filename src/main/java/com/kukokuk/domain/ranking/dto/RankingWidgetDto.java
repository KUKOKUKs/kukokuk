package com.kukokuk.domain.ranking.dto;

import com.kukokuk.domain.ranking.vo.Ranking;
import java.util.List;

/**
 * 랭킹 위젯용 데이터 DTO
 */
public class RankingWidgetDto {
    private List<Ranking> speedRankings;     // 스피드퀴즈 TOP 5
    private List<Ranking> dictationRankings; // 받아쓰기 TOP 5
    private Integer mySpeedRank;             // 내 스피드퀴즈 순위
    private Integer myDictationRank;         // 내 받아쓰기 순위

    // Getters and Setters
    public List<Ranking> getSpeedRankings() {
        return speedRankings;
    }

    public void setSpeedRankings(List<Ranking> speedRankings) {
        this.speedRankings = speedRankings;
    }

    public List<Ranking> getDictationRankings() {
        return dictationRankings;
    }

    public void setDictationRankings(List<Ranking> dictationRankings) {
        this.dictationRankings = dictationRankings;
    }

    public Integer getMySpeedRank() {
        return mySpeedRank;
    }

    public void setMySpeedRank(Integer mySpeedRank) {
        this.mySpeedRank = mySpeedRank;
    }

    public Integer getMyDictationRank() {
        return myDictationRank;
    }

    public void setMyDictationRank(Integer myDictationRank) {
        this.myDictationRank = myDictationRank;
    }
}