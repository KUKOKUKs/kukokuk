package com.kukokuk.domain.rank.service;

import com.kukokuk.common.util.DateUtil;
import com.kukokuk.domain.rank.dto.RankProcessingDto;
import com.kukokuk.domain.rank.dto.RankRequestDto;
import com.kukokuk.domain.rank.dto.RanksResponseDto;
import com.kukokuk.domain.rank.mapper.RankMapper;
import com.kukokuk.domain.rank.vo.Rank;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * 월별 랭킹 관련 서비스
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class RankService {

    private final RankMapper rankMapper;

    /**
     * 당월의 컨텐츠별 랭크 정보 등록/수정(누적)
     * @param rankProcessingDto 등록/수정할 랭크 정보
     */
    public void rankProcessing(RankProcessingDto rankProcessingDto) {
        log.info("rankProcessing() 서비스 실행 rankProcessingDto: {}", rankProcessingDto);

        // 오늘 날짜 가져오기
        String today = DateUtil.getToday("yyyy-MM");

        // 필요한 값 추출
        int userNo = rankProcessingDto.getUserNo();
        String contentType = rankProcessingDto.getContentType();
        BigDecimal score = rankProcessingDto.getScore();

        // 해당하는 정보가 이미 등록되어있는지 확인
        Rank savedRank = rankMapper.getContentRankByUserNo(
            RankRequestDto.builder()
                .userNo(userNo)
                .rankMonth(today)
                .contentType(contentType)
                .build()
        );

        if (savedRank == null) {
            // 정보가 없을 경우 최초 등록
            rankMapper.insertRank(
                Rank.builder()
                    .contentType(contentType)
                    .playCount(1)
                    .totalScore(score)
                    .rankMonth(today)
                    .userNo(userNo)
                    .build()
            );
        } else {
            // 이미 정보가 있을 경우 누적 업데이트
            // 기존 값
            int currentPlayCount = savedRank.getPlayCount();
            BigDecimal currentTotalScore = savedRank.getTotalScore();
            
            // 누적 값
            int newPlayCount = currentPlayCount + 1;
            BigDecimal cumulative = currentTotalScore
                .multiply(BigDecimal.valueOf(currentPlayCount)) // 곱하기
                .add(score); // 누적 합계
            BigDecimal newAverage = cumulative
                // 분모, 계산 중 유지할 소수 자릿수의 최대 길이(정밀도를 위해 10자리), 반올림 방식
                .divide(BigDecimal.valueOf(newPlayCount), 10, RoundingMode.HALF_UP)
                // 최종 결과 소수점 5자리 반올림
                .setScale(5, RoundingMode.HALF_UP);
            log.info(
                "({}) 누적 total_score(평균값) 계산: 기존[{}], 누적[{}]"
                , today, currentTotalScore, newAverage
            );

            savedRank.setPlayCount(newPlayCount);
            savedRank.setTotalScore(newAverage);

            // 누적 업데이트
            rankMapper.updateRank(savedRank);
        }
    }

    /**
     * 조건에 해당하며 사용자 랭크를 포함한 랭크 목록 조회
     * (groupNo 입력 여부에 따라 그룹/일반 컨텐츠별 랭크 목록 조회 요청)
     * <p>
     *     정확한 데이터를 가져오기 위해 RANK() 사용으로 limit 개수 보다 많을 수 있음 서비스단에서 가공 필요
     * @param rankRequestDto 랭크 조회 조건 정보 DTO
     * @return RanksResponseDto 컨텐츠타입과 랭크 목록 정보(userRank 정렬)
     */
    public RanksResponseDto getContentRanksIncludeUserByMonth(RankRequestDto rankRequestDto) {
        log.info("getContentRanksIncludeUserByMonth() 서비스 실행 rankRequestDto: {}", rankRequestDto);

        // DB에서 RANK() 적용하여 사용자 랭크를 포함한 랭크 목록 조회
        List<Rank> fetchRanks;
        Integer groupNo = rankRequestDto.getGroupNo();

        if (groupNo != null) {
            // 그룹 랭크 조회일 경우
            log.info("그룹 컨텐츠별 랭크 조회 groupNo: {}", groupNo);
            fetchRanks = rankMapper.getGroupContentRanksIncludeUserByMonth(rankRequestDto);
        } else {
            // 그룹 랭크 조회가 아닐 경우
            log.info("컨텐츠별 랭크 조회");
            fetchRanks = rankMapper.getContentRanksIncludeUserByMonth(rankRequestDto);
        }

        // 가공 메소드 호출하여 limit 유지 + 내 순위 포함 처리한 리스트 적용한 RanksResponseDto 반환
        return RanksResponseDto.builder()
            .contentType(rankRequestDto.getContentType())
            .ranks(
                processRanksIncludeUserRank(
                    fetchRanks
                    , rankRequestDto.getUserNo()
                    , rankRequestDto.getLimit()
                )
            )
            .build();
    }

    /**
     * 현 서비스단에서만 사용될 가공로직을 수행하는 메소드
     * 리스트를 limit 개수로 잘라내고, 사용자 순위(userNo) 포함 처리
     * - limit 외 순위는 마지막 데이터로 교체
     * - limit 내 순위는 그대로
     * - 순위가 없을 경우 잘라낸 리스트
     * @param ranks 가공할 Rank 리스트
     * @param userNo 사용자 번호
     * @param limit 가공할 리스트 개수
     * @return 가공된 Rank 리스트
     */
    private List<Rank> processRanksIncludeUserRank(List<Rank> ranks, int userNo, int limit) {
        log.info("processRanksIncludeUser() 서비스 실행");

        // 사용자 Rank 데이터 추출
        Rank myRank = ranks.stream()
            .filter(r -> r.getUserNo() == userNo)
            .findFirst()
            .orElse(null);

        // 리스트를 limit 개수로 잘라내기
        if (ranks.size() > limit) {
            ranks = ranks.subList(0, limit);
        }

        // 사용자 순위가 limit 외에 있는 경우 마지막 데이터를 내 순위로 교체
        if (myRank != null && !ranks.contains(myRank)) {
            ranks.set(limit - 1, myRank);
        }

        return ranks;
    }

    /**
     * 레벨 기준 사용자를 포함한 상위 랭크 목록 조회
     * <p>
     *     날짜 상관없이 레벨과 경험치로만 정렬
     *     정확한 데이터를 가져오기 위해 RANK() 사용으로 limit 개수 보다 많을 수 있음
     *     서비스단에서 가공 필요
     * @param userNo 사용자 번호
     * @param limit 조회할 개수
     * @return 레벨 랭크 목록 정보(level DESC, experiencePoints DESC 정렬)
     */
    public List<Rank> getLevelRanksIncludeUser(int userNo, int limit) {
        log.info("getLevelRanksIncludeUser() 서비스 실행 userNo: {}, limit: {}", userNo, limit);

        // RankRequestDto 생성
        RankRequestDto rankRequestDto = RankRequestDto.builder()
            .userNo(userNo)
            .limit(limit)
            .build();

        // DB에서 RANK() 적용하여 사용자를 포함한 레벨 랭크 목록 조회
        List<Rank> fetchRanks = rankMapper.getLevelRanksIncludeUser(rankRequestDto);

        // 가공 메소드 호출하여 limit 유지 + 내 순위 포함 처리
        return processRanksIncludeUserRank(fetchRanks, userNo, limit);
    }
}