package com.kukokuk.domain.exp.mapper;

import com.kukokuk.domain.exp.dto.ExpAggregateDto;
import com.kukokuk.domain.exp.dto.ExpProcessingDto;
import com.kukokuk.domain.exp.vo.ExpLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExpMapper {

    /**
     * 리스트에 대한 사용자 경험치 획정 정보 등록
     * @param contentType 컨텐츠타입
     * @param expGained 일괄 처리할 경험치 값
     * @param expProcessingDtos 사용자의 경험치 정보 리스트(userNo, contentNo pair를 위한 리스트)
     */
    void insertExpLogs(@Param("contentType") String contentType
        , @Param("expGained") Integer expGained
        , @Param("expProcessingDtos") List<ExpProcessingDto> expProcessingDtos);

    /**
     * 사용자 경험치 획득 정보 등록
     * @param expLog 사용자 경험치 획득 정보
     */
    void insertExpLog(ExpLog expLog);

    /**
     * 사용자의 특정 컨텐츠 타입의 오늘에 대한 경험치 횟수 조회
     * @param contentType 컨텐츠 타입
     * @param userNo 사용자 번호
     * @return 경험치 횟수
     */
    int getTodayCountExpByTypeWithUserNo(@Param("contentType") String contentType
        , @Param("userNo") int userNo);

    /**
     * 사용자의 특정 컨텐츠 타입의 오늘에 대한 경험치 합계 조회
     * @param contentType 컨텐츠 타입
     * @param userNo 사용자 번호
     * @return 경험치 합계
     */
    int getTodayTotalExpByTypeWithUserNo(@Param("contentType") String contentType
        , @Param("userNo") int userNo);
    
    /**
     * 사용자 번호로 오늘에 대한 경험치, 횟수 집계(content_type 별 집계)
     * @param userNo 사용자 정보
     * @return 오늘 경험치, 횟수 집계
     */
    List<ExpAggregateDto> getTodayExpAggregateByUserNo(int userNo);

}
