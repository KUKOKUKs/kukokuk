package com.kukokuk.mapper;

import com.kukokuk.vo.MaterialParseJob;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MaterialParseJobMapper {

    // 작업 현황을 생성
    void insertParseJob(MaterialParseJob materialParseJob);

    // 파라미터로 받은 url중에 이미 DB에 존재하는 url만 조회
    List<String> getExistUrls(List<String> allUrls);

    // jobNo 식별자를 이용해 MaterialParseJob (자료파싱작업 현황) 데이터를 조회
    MaterialParseJob getParseJobByNo(int jobNo);

    // MaterialParseJob의 상태를 IN_PROGRESS로 업데이트
    void updateParseJobStatusToInProgress(int jobNo);

    // MaterialParseJob의 상태를 SUCCESS로 업데이트 + 생성된 학습자료 식별자를 컬럼에 추가
    void updateParseJobStatusToSuccess(@Param("jobNo") int jobNo,
        @Param("dailyStudyMaterialNo") int dailyStudyMaterialNo);

    void updateParseJobStatusToFailed(@Param("jobNo") int jobNo, @Param("message") String message);

    // 자료파싱작업 목록을 생성된 학습자료와 함께 조회
    List<MaterialParseJob> getParseJobsWithMaterial(int rows);
}
