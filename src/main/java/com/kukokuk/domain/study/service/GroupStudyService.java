package com.kukokuk.domain.study.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.common.dto.JobStatusResponse;
import com.kukokuk.common.exception.AppException;
import com.kukokuk.common.service.ObjectStorageService;
import com.kukokuk.common.store.RedisJobStatusStore;
import com.kukokuk.domain.study.dto.DailyStudySummaryResponse;
import com.kukokuk.domain.study.dto.GroupParseMaterialResponse;
import com.kukokuk.domain.study.dto.StudyMaterialJobPayload;
import com.kukokuk.domain.study.mapper.DailyStudyMaterialMapper;
import com.kukokuk.domain.study.vo.DailyStudyMaterial;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class GroupStudyService {

    private final ObjectStorageService objectStorageService;
    private final DailyStudyMaterialMapper dailyStudyMaterialMapper;
    private final RedisJobStatusStore<GroupParseMaterialResponse> studyJobStatusStore;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 그룹의 학습자료 업로드 요청을 처리하는 서비스
     * 파일을 스토리지에 저장 후, Redis 작업큐에 파일정보를 push하면
     * 파이썬 워커가 해당 작업큐의 데이터로 파일에서 텍스트를 추출하여 반환(콜백 API 호출)
     * - 파일을 Object Storage에 저장
     * - DB의 dailyStudyMaterials 테이블에 데이터 추가 (원본파일명, 스토리지 경로)
     * - JobStatusResponse 객체 생성 및 Redis 상태저장소에 저장
     * - Redis 작업 큐에 push
     * @param files
     * @param groupId
     * @param difficulty
     */
    public void uploadGroupMaterials(List<MultipartFile> files, int groupId, int difficulty) {
        for (MultipartFile file : files) {
            try {
                // 1. Object Storage에 자료 업로드
                String fileUrl = objectStorageService.uploadGroupMaterial(file, groupId);

                // 2. DB에 파일 정보 저장 (dailyStudyMaterials테이블)
                String filename =  file.getOriginalFilename();

                DailyStudyMaterial material = new DailyStudyMaterial();
                material.setMaterialTitle(filename);
                material.setSourceFilename(fileUrl);

                dailyStudyMaterialMapper.insertStudyMaterial(material);

                // 3. jobid 생성
                String jobId = String.format("material:%d:difficulty:%d",
                    material.getDailyStudyMaterialNo(),
                    difficulty);

                // 4. JobStatusResponse 생성 및 Redis 저장
                // Job 상태 객체 생성 (PROCESSING 상태)
                JobStatusResponse<GroupParseMaterialResponse> jobStatus = JobStatusResponse.<GroupParseMaterialResponse>builder()
                    .jobId(jobId)
                    .status("PENDING")
                    .progress(0)
                    .result(null)
                    .message("파일 업로드 완료, 텍스트 추출 대기 중")
                    .build();
                // Redis 상태저장소에 저장
                studyJobStatusStore.put(jobStatus);

                // 5. 작업큐에 push
                StudyMaterialJobPayload payload = StudyMaterialJobPayload.builder()
                    .jobId(jobId)
                    .fileUrl(fileUrl)
                    .groupId(groupId)
                    .difficulty(difficulty)
                    .dailyStudyMaterialNo(material.getDailyStudyMaterialNo())
                    .build();
                String payloadJson = objectMapper.writeValueAsString(payload);

                stringRedisTemplate.opsForList().leftPush("queue:material:group", payloadJson);

            } catch (IOException e) {
                throw new AppException("첨부파일 저장 중 오류가 발생하였습니다");
            }
        }

    }
}
