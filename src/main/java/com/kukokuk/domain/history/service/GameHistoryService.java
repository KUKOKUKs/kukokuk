package com.kukokuk.domain.history.service;

import com.kukokuk.domain.history.dto.GameHistoryDto;
import com.kukokuk.domain.history.mapper.GameHistoryMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class GameHistoryService {

    private final GameHistoryMapper gameHistoryMapper;

    /**
     * 스피드 퀴즈 최근 이력 조회
     * @param userNo 유저 번호
     * @param limit 조회 개수
     * @return 스피드 퀴즈 이력 리스트
     */
    public List<GameHistoryDto> getRecentSpeedHistory(int userNo, int limit) {
        log.info("[Service] 스피드 퀴즈 이력 조회 - userNo: {}, limit: {}", userNo, limit);

        try {
            List<GameHistoryDto> historyList = gameHistoryMapper.getRecentSpeedHistory(userNo, limit);
            log.info("[Service] 스피드 퀴즈 이력 조회 성공 - 조회된 개수: {}", historyList.size());
            return historyList;
        } catch (Exception e) {
            log.error("[Service] 스피드 퀴즈 이력 조회 실패 - userNo: {}", userNo, e);
            throw new RuntimeException("스피드 퀴즈 이력 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 단계별 퀴즈 최근 이력 조회
     * @param userNo 유저 번호
     * @param limit 조회 개수
     * @return 단계별 퀴즈 이력 리스트
     */
    public List<GameHistoryDto> getRecentLevelHistory(int userNo, int limit) {
        log.info("[Service] 단계별 퀴즈 이력 조회 - userNo: {}, limit: {}", userNo, limit);

        try {
            List<GameHistoryDto> historyList = gameHistoryMapper.getRecentLevelHistory(userNo, limit);
            log.info("[Service] 단계별 퀴즈 이력 조회 성공 - 조회된 개수: {}", historyList.size());
            return historyList;
        } catch (Exception e) {
            log.error("[Service] 단계별 퀴즈 이력 조회 실패 - userNo: {}", userNo, e);
            throw new RuntimeException("단계별 퀴즈 이력 조회 중 오류가 발생했습니다.", e);
        }
    }

}