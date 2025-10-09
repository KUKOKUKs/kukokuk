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
        String today = DateUtil.getToday("yyyy-mm");

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

//    /**
//     * 컨텐츠 타입 코드를 한글명으로 변환
//     */
//    private String getContentTypeName(String contentType) {
//        switch (contentType) {
//            case "SPEED":
//                return "스피드퀴즈";
//            case "LEVEL":
//                return "단계별퀴즈";
//            case "DICTATION":
//                return "받아쓰기";
//            default:
//                return "알 수 없음";
//        }
//    }
//
//    /**
//     * 새로운 월별 랭킹 등록 (첫 플레이)
//     * @param contentType 컨텐츠 타입
//     * @param score 계산된 점수
//     * @param userNo 사용자 번호
//     */
//    @Transactional
//    public void insertMonthlyRanking(String contentType, double score, int userNo) {
//        String currentMonth = RankingUtil.getCurrentMonth();
//        log.info("insertMonthlyRanking 서비스 실행 - 사용자: {}, 컨텐츠: {}, 점수: {}, 월: {}",
//            userNo, contentType, score, currentMonth);
//
//        Ranking ranking = new Ranking();
//        ranking.setContentType(contentType);
//        ranking.setPlayCount(1);
//        ranking.setTotalScore(BigDecimal.valueOf(score));
//        ranking.setRankMonth(currentMonth);
//        ranking.setUserNo(userNo);
//
//        rankingMapper.insertRanking(ranking);
//        log.info("신규 월별 랭킹 등록 완료 - 랭킹번호: {}, 월: {}", ranking.getRankNo(), currentMonth);
//    }
//
//    /**
//     * 기존 월별 랭킹 업데이트 (재플레이 시 누적 평균 계산)
//     * @param rankNo 기존 랭킹 번호
//     * @param newScore 새로운 점수
//     */
//    @Transactional
//    public void updateMonthlyRanking(int rankNo, double newScore) {
//        log.info("updateMonthlyRanking 서비스 실행 - 랭킹번호: {}, 새점수: {}", rankNo, newScore);
//
//        Ranking existingRanking = rankingMapper.getRankingByRankNo(rankNo);
//        if (existingRanking == null) {
//            log.error("랭킹 정보를 찾을 수 없음: {}", rankNo);
//            throw new IllegalArgumentException("랭킹 정보를 찾을 수 없습니다.");
//        }
//
//        int currentPlayCount = existingRanking.getPlayCount();
//        double currentTotalScore = existingRanking.getTotalScore().doubleValue();
//
//        double newCumulativeScore = (currentTotalScore * currentPlayCount) + newScore;
//        int newPlayCount = currentPlayCount + 1;
//        double newAverageScore = newCumulativeScore / newPlayCount;
//
//        log.info("월별 평균 점수 계산: {}회차 {} + 새점수 {} = 평균 {} ({}월)",
//            currentPlayCount, currentTotalScore, newScore, newAverageScore,
//            existingRanking.getRankMonth());
//
//        existingRanking.setPlayCount(newPlayCount);
//        existingRanking.setTotalScore(BigDecimal.valueOf(newAverageScore));
//
//        rankingMapper.updateRanking(existingRanking);
//        log.info("월별 랭킹 업데이트 완료");
//    }
//
//    /**
//     * 월별 랭킹 처리 (기존 랭킹이 있으면 업데이트, 없으면 신규 등록)
//     * 각 컨텐츠 플레이 완료 후 호출되어야 함
//     * @param contentType 컨텐츠 타입
//     * @param score 점수
//     * @param userNo 사용자 번호
//     */
//    @Transactional
//    public void processMonthlyRanking(String contentType, double score, int userNo) {
//        String currentMonth = RankingUtil.getCurrentMonth();
//        log.info("processMonthlyRanking 서비스 실행 - 사용자: {}, 컨텐츠: {}, 점수: {}, 현재월: {}",
//            userNo, contentType, score, currentMonth);
//
//        Ranking existingRanking = rankingMapper.getRankingByUserAndContent(userNo, contentType);
//
//        if (existingRanking == null) {
//            insertMonthlyRanking(contentType, score, userNo);
//            log.info("현재월({}) 신규 랭킹 등록 완료", currentMonth);
//        } else {
//            updateMonthlyRanking(existingRanking.getRankNo(), score);
//            log.info("현재월({}) 기존 랭킹 업데이트 완료", currentMonth);
//        }
//    }
//
//    /**
//     * 전체 월별 랭킹 목록 조회 (현재 월 기준)
//     * @param contentType 컨텐츠 타입
//     * @param limit 조회할 개수
//     * @return 전체 랭킹 목록 (점수 높은 순)
//     */
//    public List<Ranking> getGlobalRankings(String contentType, int limit) {
//        log.info("getGlobalRankings 서비스 실행 - 컨텐츠: {}, 개수: {}, 현재월: {}",
//            contentType, limit, RankingUtil.getCurrentMonth());
//        return rankingMapper.getGlobalRankings(contentType, limit);
//    }
//
//    /**
//     * 전체 월별 랭킹 목록 조회 (특정 월 기준)
//     * @param contentType 컨텐츠 타입
//     * @param rankMonth 조회할 월 (YYYY-MM 형태)
//     * @param limit 조회할 개수
//     * @return 전체 랭킹 목록 (점수 높은 순)
//     */
//    public List<Ranking> getGlobalRankingsByMonth(String contentType, String rankMonth, int limit) {
//        log.info("getGlobalRankingsByMonth 서비스 실행 - 컨텐츠: {}, 월: {}, 개수: {}",
//            contentType, rankMonth, limit);
//        return rankingMapper.getGlobalRankingsByMonth(contentType, rankMonth, limit);
//    }
//
//    /**
//     * 전체 월별 랭킹 목록을 DTO로 변환하여 조회 (현재 월 기준)
//     * @param contentType 컨텐츠 타입 코드
//     * @param limit 조회할 개수
//     * @return 랭킹 DTO 목록
//     */
//    public List<RankingDto> getGlobalRankingDtos(String contentType, int limit) {
//        List<Ranking> rankings = getGlobalRankings(contentType, limit);
//        String typeName = getContentTypeName(contentType);
//
//        return rankings.stream()
//            .map(ranking -> new RankingDto(ranking, typeName))
//            .collect(Collectors.toList());
//    }
//
//    /**
//     * 전체 월별 랭킹 목록을 DTO로 변환하여 조회 (특정 월 기준)
//     * @param contentType 컨텐츠 타입 코드
//     * @param rankMonth 조회할 월
//     * @param limit 조회할 개수
//     * @return 랭킹 DTO 목록
//     */
//    public List<RankingDto> getGlobalRankingDtosByMonth(String contentType, String rankMonth, int limit) {
//        List<Ranking> rankings = getGlobalRankingsByMonth(contentType, rankMonth, limit);
//        String typeName = getContentTypeName(contentType);
//
//        return rankings.stream()
//            .map(ranking -> new RankingDto(ranking, typeName))
//            .collect(Collectors.toList());
//    }
//
//    /**
//     * 그룹 내 월별 랭킹 목록 조회 (현재 월 기준)
//     * @param contentType 컨텐츠 타입
//     * @param groupNo 그룹 번호
//     * @param limit 조회할 개수
//     * @return 그룹 내 랭킹 목록 (점수 높은 순)
//     */
//    public List<Ranking> getGroupRankings(String contentType, int groupNo, int limit) {
//        log.info("getGroupRankings 서비스 실행 - 컨텐츠: {}, 그룹: {}, 개수: {}, 현재월: {}",
//            contentType, groupNo, limit, RankingUtil.getCurrentMonth());
//        return rankingMapper.getGroupRankings(contentType, groupNo, limit);
//    }
//
//    /**
//     * 그룹 내 월별 랭킹 목록 조회 (특정 월 기준)
//     * @param contentType 컨텐츠 타입
//     * @param groupNo 그룹 번호
//     * @param rankMonth 조회할 월 (YYYY-MM 형태)
//     * @param limit 조회할 개수
//     * @return 그룹 내 랭킹 목록 (점수 높은 순)
//     */
//    public List<Ranking> getGroupRankingsByMonth(String contentType, int groupNo, String rankMonth, int limit) {
//        log.info("getGroupRankingsByMonth 서비스 실행 - 컨텐츠: {}, 그룹: {}, 월: {}, 개수: {}",
//            contentType, groupNo, rankMonth, limit);
//        return rankingMapper.getGroupRankingsByMonth(contentType, groupNo, rankMonth, limit);
//    }
//
//    /**
//     * 전체 랭킹에서 사용자 순위 조회 (현재 월 기준)
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @return 전체 순위 (1부터 시작)
//     */
//    public int getGlobalUserRanking(int userNo, String contentType) {
//        log.info("getGlobalUserRanking 서비스 실행 - 사용자: {}, 컨텐츠: {}, 현재월: {}",
//            userNo, contentType, RankingUtil.getCurrentMonth());
//        return rankingMapper.getGlobalUserRanking(userNo, contentType);
//    }
//
//    /**
//     * 전체 랭킹에서 사용자 순위 조회 (특정 월 기준)
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @param rankMonth 조회할 월 (YYYY-MM 형태)
//     * @return 전체 순위 (1부터 시작)
//     */
//    public int getGlobalUserRankingByMonth(int userNo, String contentType, String rankMonth) {
//        log.info("getGlobalUserRankingByMonth 서비스 실행 - 사용자: {}, 컨텐츠: {}, 월: {}",
//            userNo, contentType, rankMonth);
//        return rankingMapper.getGlobalUserRankingByMonth(userNo, contentType, rankMonth);
//    }
//
//    /**
//     * 사용자의 특정 컨텐츠 현재 월 랭킹 조회
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @return 랭킹 정보, 없으면 null
//     */
//    public Ranking getRankingByUserAndContent(int userNo, String contentType) {
//        log.info("getRankingByUserAndContent 서비스 실행 - 사용자: {}, 컨텐츠: {}, 현재월: {}",
//            userNo, contentType, RankingUtil.getCurrentMonth());
//        return rankingMapper.getRankingByUserAndContent(userNo, contentType);
//    }
//
//    /**
//     * 사용자의 특정 컨텐츠 특정 월 랭킹 조회
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @param rankMonth 조회할 월 (YYYY-MM 형태)
//     * @return 랭킹 정보, 없으면 null
//     */
//    public Ranking getRankingByUserContentAndMonth(int userNo, String contentType, String rankMonth) {
//        log.info("getRankingByUserContentAndMonth 서비스 실행 - 사용자: {}, 컨텐츠: {}, 월: {}",
//            userNo, contentType, rankMonth);
//        return rankingMapper.getRankingByUserContentAndMonth(userNo, contentType, rankMonth);
//    }
//
//    /**
//     * 사용자의 모든 랭킹 삭제
//     * @param userNo 사용자 번호
//     */
//    @Transactional
//    public void deleteRankingsByUser(int userNo) {
//        log.info("deleteRankingsByUser 서비스 실행 - 사용자: {}", userNo);
//        rankingMapper.deleteRankingsByUser(userNo);
//        log.info("사용자 랭킹 삭제 완료");
//    }
//
//    /**
//     * 사용자의 월별 랭킹 히스토리 조회
//     * @param userNo 사용자 번호
//     * @param contentType 컨텐츠 타입
//     * @param limit 조회할 개수 (최근 월부터)
//     * @return 월별 랭킹 히스토리 목록
//     */
//    public List<Ranking> getUserMonthlyHistory(int userNo, String contentType, int limit) {
//        log.info("getUserMonthlyHistory 서비스 실행 - 사용자: {}, 컨텐츠: {}, 개수: {}",
//            userNo, contentType, limit);
//        return rankingMapper.getUserMonthlyHistory(userNo, contentType, limit);
//    }

}