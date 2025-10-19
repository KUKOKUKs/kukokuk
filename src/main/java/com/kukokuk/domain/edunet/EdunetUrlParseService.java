package com.kukokuk.domain.edunet;

import com.kukokuk.domain.study.dto.ParseMaterialRequest;
import com.kukokuk.domain.study.dto.ParseMaterialResponse;
import com.kukokuk.domain.study.mapper.DailyStudyMaterialMapper;
import com.kukokuk.domain.study.mapper.MaterialParseJobMapper;
import com.kukokuk.domain.study.vo.DailyStudyMaterial;
import com.kukokuk.domain.study.vo.MaterialParseJob;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class EdunetUrlParseService {

    private final MaterialParseJobMapper materialParseJobMapper;
    private final DailyStudyMaterialMapper dailyStudyMaterialMapper;
    private final StringRedisTemplate stringRedisTemplate;


    /**
     * 요청으로 받은 에듀넷 URL 리스트를 큐에 넣고 각각의 상태를 DB에 저장
     * 파이썬 서버를 호출해 에듀넷 url에서 hwp 추출 후 텍스트 데이터를 반환받으면, 그 텍스트를 DB에 저장
     * @param request
     * @return
     */
    @Transactional
    public ParseMaterialResponse createMaterial(ParseMaterialRequest request) {
        ParseMaterialResponse parseMaterialResponse = new ParseMaterialResponse();

        List<String> allUrls = request.getUrls();

        // 요청으로 받은 url 목록 중 이미 DB에 존재하는 url만 조회
        List<String> existingUrls = materialParseJobMapper.getExistUrls(allUrls);
        // API 반환 데이터에 skippedUrls 설정 (이미 처리된 경로라 스킵)
        parseMaterialResponse.setSkippedUrls(existingUrls);

        // db에 이미 있지 않은 신규 url만 필터링
        List<String> newUrls = allUrls.stream()
            .filter(url -> !existingUrls.contains(url))
            .toList();
        // API 반환 데이터에 enqueuedUrls 설정 (큐에 담긴후 백그라운드 작업 실행 될 url들)
        parseMaterialResponse.setEnqueuedUrls(newUrls);

        for (String fileUrl : newUrls) {

            MaterialParseJob materialParseJob = new MaterialParseJob();
            materialParseJob.setUrl(fileUrl);

            // 각 에듀넷 링크를 PARSE_JOB_STATUS 테이블에 저장
            materialParseJobMapper.insertParseJob(materialParseJob);

            try {
                // Redis에 넣을 JSON 형태의 메시지 생성 (jobId + url 포함)
                String jobPayload = String.format("{\"jobNo\":%d,\"url\":\"%s\"}",
                    materialParseJob.getMaterialParseJobNo(),
                    fileUrl);

                // 레디스에 URL 하나씩 푸시
                ListOperations<String, String> listOperations = stringRedisTemplate.opsForList();
                listOperations.rightPush("parse:queue:admin", jobPayload);
            } catch (Exception e) {
                log.error("Redis push 실패: jobNo={}, url={}, error={}",
                    materialParseJob.getMaterialParseJobNo(), fileUrl, e.getMessage());

                // 레디스 푸시실패시 해당 job을 Failed로 표시
                materialParseJobMapper.updateParseJobStatusToFailed(materialParseJob.getMaterialParseJobNo(),
                    e.getMessage());
            }
        }

        return parseMaterialResponse;
    }


    /**
     * 관리자 파이썬 워커 성공 콜백 처리
     *
     * - 파이썬 워커가 전달한 파싱 결과를 DB에 저장하고
     * - 해당 job의 상태를 SUCCESS로 업데이트
     *
     * @param request 파싱 완료 결과 (jobNo, content, school, grade, title 등)
     */
    public void handleAdminWorkerCallback(WorkerAdminCallbackRequest request) {
        int jobNo = request.getJobNo();

        try {
            // 파싱된 결과를 DailyStudyMaterial 테이블에 저장
            DailyStudyMaterial material = DailyStudyMaterial.builder()
                .content(request.getContent())
                .grade(request.getGrade())
                .school(request.getSchool())
                .materialTitle(request.getTitle())
                .keywords(request.getKeywords())
                .sourceFilename(request.getSourceFilename())
                .build();

            // 해당 school, grade에서의 마지막 sequence 값을 조회
            int sequence = dailyStudyMaterialMapper.getMaxSequenceBySchoolAndGrade(
                material.getSchool(), material.getGrade());
            material.setSequence(sequence + 1);

            // sequence 값은 해당 school과 grade로 mapper내에서 계산해서 저장됨
            dailyStudyMaterialMapper.insertStudyMaterial(material);

            // jobId로 해당 job의 status를 SUCCESS로 업데이트하기
            // - MaterialParseJob를 업데이트
            materialParseJobMapper.updateParseJobStatusToSuccess(jobNo,
                material.getDailyStudyMaterialNo());

            log.info("[관리자 파싱 콜백 완료] jobNo={}, materialNo={}",
                jobNo, material.getDailyStudyMaterialNo());
        } catch (Exception e) {
            // 실패 시 job 상태 FAILED로 변경
            materialParseJobMapper.updateParseJobStatusToFailed(jobNo, e.getMessage());
            log.error("[관리자 파싱 콜백 실패] jobNo={} error={}", jobNo, e.getMessage(), e);
        }
    }


    /**
     * 관리자 파이썬 워커 실패 콜백 처리
     *
     * - 워커 측에서 예외 발생 시 호출되며
     * - 해당 job의 상태를 FAILED로 업데이트
     *
     * @param request 실패 job의 식별자와 에러 메시지
     */
    public void handleAdminWorkerFailCallback(WorkerAdminCallbackRequest request) {
        int jobNo = request.getJobNo();
        String error = request.getError();

        materialParseJobMapper.updateParseJobStatusToFailed(jobNo, error);
        log.warn("[관리자 파싱 실패 콜백 수신] jobNo={}, error={}", jobNo, error);
    }

}
