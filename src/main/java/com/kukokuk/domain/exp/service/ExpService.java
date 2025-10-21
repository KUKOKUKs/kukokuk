package com.kukokuk.domain.exp.service;

import com.kukokuk.domain.exp.dto.ExpAggregateDto;
import com.kukokuk.domain.exp.mapper.ExpMapper;
import com.kukokuk.domain.exp.vo.ExpLog;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor // 초기화 되지않은 final 필드나, @NonNull 이 붙은 필드에 대해 생성자를 생성
public class ExpService {

    private final ExpMapper expMapper;

    /**
     * 사용자 경험치 획득 정보 등록
     * @param expLog 사용자 경험치 획득 정보
     */
    public void insertExpLog(ExpLog expLog) {
        log.info("insertExpLog() 서비스 실행");
        expMapper.insertExpLog(expLog);
    }

    /**
     * 사용자의 특정 컨텐츠 타입의 오늘에 대한 경험치 횟수 조회
     * @param contentType 컨텐츠 타입
     * @param userNo 사용자 번호
     * @return 경험치 횟수
     */
    public int getTodayCountExpByTypeWithUserNo(
        @Param("contentType") String contentType
        , @Param("userNo") int userNo) {
        log.info("getTodayCountExpByTypeWithUserNo() 서비스 실행");
        return expMapper.getTodayCountExpByTypeWithUserNo(contentType, userNo);
    }

    /**
     * 사용자의 특정 컨텐츠 타입의 오늘에 대한 경험치 합계 조회
     * @param contentType 컨텐츠 타입
     * @param userNo 사용자 번호
     * @return 경험치 합계
     */
    public int getTodayTotalExpByTypeWithUserNo(
        @Param("contentType") String contentType
        , @Param("userNo") int userNo) {
        log.info("getTodayTotalExpByTypeWithUserNo() 서비스 실행");
        return expMapper.getTodayTotalExpByTypeWithUserNo(contentType, userNo);
    }

    /**
     * 사용자 번호로 오늘에 대한 경험치, 횟수 집계(CONTENT_TYPE 기준)
     * @param userNo 사용자 정보
     * @return 오늘 경험치, 횟수 집계
     */
    public List<ExpAggregateDto> getTodayExpAggregateByUserNo(int userNo) {
        log.info("getTodayExpAggregateByUserNo() 서비스 실행");
        return expMapper.getTodayExpAggregateByUserNo(userNo);
    }

}
