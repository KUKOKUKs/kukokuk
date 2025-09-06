package com.kukokuk.domain.ranking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 각 컨텐츠별 점수 계산을 위한 데이터 조회 Mapper
 */
@Mapper
public interface ScoreCalculationMapper {

    /**
     * 동일 정답수 그룹의 최대 소요 시간 조회
     *
     * @param correctAnswers 정답 수
     * @return 최대 시간 (초)
     */
    Float getMaxTimeByCorrectAnswers(@Param("correctAnswers") int correctAnswers);

    /**
     * 동일 정답수 그룹의 최소 소요 시간 조회
     *
     * @param correctAnswers 정답 수
     * @return 최소 시간 (초)
     */
    Float getMinTimeByCorrectAnswers(@Param("correctAnswers") int correctAnswers);

    // 추후 받아쓰기, 학습 등 다른 컨텐츠의 점수 계산 메서드들 추가 예정
}