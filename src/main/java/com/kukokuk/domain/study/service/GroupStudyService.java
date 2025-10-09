package com.kukokuk.domain.study.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.common.dto.JobStatusResponse;
import com.kukokuk.common.exception.AppException;
import com.kukokuk.common.service.ObjectStorageService;
import com.kukokuk.common.store.RedisJobStatusStore;
import com.kukokuk.domain.study.dto.DailyStudySummaryResponse;
import com.kukokuk.domain.study.dto.GroupParseMaterialResponse;
import com.kukokuk.domain.study.dto.StudyMaterialJobPayload;
import com.kukokuk.domain.study.dto.TeacherDailyStudyResponse;
import com.kukokuk.domain.study.dto.UserStudyRecommendationDto;
import com.kukokuk.domain.study.mapper.DailyStudyMapper;
import com.kukokuk.domain.study.mapper.DailyStudyMaterialMapper;
import com.kukokuk.domain.study.mapper.GroupStudyMapper;
import com.kukokuk.domain.study.vo.DailyStudy;
import com.kukokuk.domain.study.vo.DailyStudyMaterial;
import com.kukokuk.domain.user.vo.User;
import com.kukokuk.integration.redis.WorkerMaterialCallbackRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
@RequiredArgsConstructor
public class GroupStudyService {

    private final ObjectStorageService objectStorageService;
    private final DailyStudyMaterialMapper dailyStudyMaterialMapper;
    private final DailyStudyMapper dailyStudyMapper;
    private final GroupStudyMapper groupStudyMapper;
    private final RedisJobStatusStore<GroupParseMaterialResponse> studyJobStatusStore;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final StudyService studyService;

    /**
     * 그룹의 학습자료 업로드 요청을 처리하는 서비스
     * 파일을 스토리지에 저장 후, Redis 작업큐에 파일정보를 push하면
     * 파이썬 워커가 해당 작업큐의 데이터로 파일에서 텍스트를 추출하여 반환(콜백 API 호출)
     * - 파일을 Object Storage에 저장
     * - DB의 dailyStudyMaterials 테이블에 데이터 추가 (원본파일명, 스토리지 경로)
     * - JobStatusResponse 객체 생성 및 Redis 상태저장소에 저장
     * - Redis 작업 큐에 push
     * @param files
     * @param groupNo
     * @param difficulty
     */
    public List<String> uploadGroupMaterials(List<MultipartFile> files, int groupNo, int difficulty) {
        List<String> jobIdList = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // 1. Object Storage에 자료 업로드
                String fileUrl = objectStorageService.uploadGroupMaterial(file, groupNo);

                // 2. DB에 파일 정보 저장 (dailyStudyMaterials테이블)
                String filename =  file.getOriginalFilename();

                DailyStudyMaterial material = new DailyStudyMaterial();
                material.setSourceFilename(filename);
                material.setFilePath(fileUrl);
                material.setGroupNo(groupNo);
                int lastSequence = groupStudyMapper.getMaxSequenceByGroupNo(groupNo);
                material.setSequence(lastSequence + 1);

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
                    .progress(20)
                    .result(null)
                    .message("파일 업로드 완료, 텍스트 추출 대기 중")
                    .build();
                // Redis 상태저장소에 저장
                studyJobStatusStore.put(jobStatus);

                // group:{groupNo}:jobs 세트에 jobId 추가 (보조 인덱스)
                stringRedisTemplate.opsForSet().add("group:" + groupNo + ":jobs", jobId);

                // 여기서 Set에도 TTL 적용
                // 그룹 내 작업이 계속 들어오면 set이 유지되고, 일정 시간(15분) 동안 아무 작업도 없으면 자동 삭제
                stringRedisTemplate.expire("group:" + groupNo + ":jobs", Duration.ofMinutes(15));

                // 5. 작업큐에 push
                StudyMaterialJobPayload payload = StudyMaterialJobPayload.builder()
                    .jobId(jobId)
                    .fileUrl(fileUrl)
                    .groupNo(groupNo)
                    .difficulty(difficulty)
                    .dailyStudyMaterialNo(material.getDailyStudyMaterialNo())
                    .build();
                String payloadJson = objectMapper.writeValueAsString(payload);

                stringRedisTemplate.opsForList().leftPush("queue:material:group", payloadJson);

                jobIdList.add(jobId);
            } catch (IOException e) {
                throw new AppException("첨부파일 저장 중 오류가 발생하였습니다");
            }
        }

        return jobIdList;
    }


    public void handleWorkerCallback(WorkerMaterialCallbackRequest request) {
        int dailyStudyMaterialNo = request.getDailyStudyMaterialNo();

        // 1. 추출된 텍스트를 dailyStudyMaterial의 content에 저장
        DailyStudyMaterial material = DailyStudyMaterial.builder()
            .dailyStudyMaterialNo(dailyStudyMaterialNo)
            .content(request.getContent())
            .build();
        dailyStudyMaterialMapper.updateStudyMaterial(material);

        // 2. 작업상태 업데이트
        studyJobStatusStore.update(request.getJobId(), status -> {
            status.setStatus("IN_PROGRESS");
            status.setProgress(50);
            status.setResult(null);
            status.setMessage("텍스트 추출 완료 및 AI 재가공 진행중 ");
        });

        // 3. difficulty가 null이 아니면, LLM AI 호출 및 재가공 자료 DB에 저장
        if(request.getDifficulty() != null) {
            DailyStudy dailyStudy = studyService.createDailyStudyByAi(dailyStudyMaterialNo, request.getDifficulty());

            // dailyStudy에 groupNo 추가
            dailyStudyMapper.updateGroupNo(dailyStudy.getDailyStudyNo(), request.getGroupNo());
        }


        // dailyStudyMaterialNo로 material 조회
        material = dailyStudyMaterialMapper.getStudyMaterialByNo(dailyStudyMaterialNo);

        // JobStatus에 담을 프론트 반환 값 생성
        GroupParseMaterialResponse response = new GroupParseMaterialResponse();
        response.setDailyStudyMaterialNo(dailyStudyMaterialNo);
        response.setMaterialTitle(material.getMaterialTitle());
        response.setDifficulty(request.getDifficulty());
        response.setSourceFilename(material.getSourceFilename());

        // 4. 최종 작업 완료 후 작업상태 DONE으로 업데이트
        studyJobStatusStore.update(request.getJobId(), status -> {
            status.setStatus("DONE");
            status.setProgress(100);
            status.setResult(response);
            status.setMessage("파일 업로드 및 일일 학습 자료 생성 완료");
        });
    }


    public List<JobStatusResponse<GroupParseMaterialResponse>> getAllJobsByGroup(int groupNo) {
        List<JobStatusResponse<GroupParseMaterialResponse>> jobList = new ArrayList<>();
        // 해당 그룹의 jobId들을 담은 set을 조회
        Set<String> jobIds = stringRedisTemplate.opsForSet().members("group:" + groupNo + ":jobs");

        if (jobIds.isEmpty()) return jobList;

        jobList = jobIds.stream()
            .map(studyJobStatusStore::get) // jobId -> studyJobStatusStore.get(jobId)
            .filter(Objects::nonNull) // jobId가 Redis에서 만료되어 null로 조회되면 리스트에 넣지 않음 status -> status != null
            .toList();

        return jobList;
    }

    public JobStatusResponse<GroupParseMaterialResponse> getJobStatus(String jobId) {
        return studyJobStatusStore.get(jobId);
    }

    // 교사 화면에서 업로드한 파일을 기반으로 재가공된 일일학습자료 목록을 조회
    public List<TeacherDailyStudyResponse> getTeacherGroupDailyStudies(int groupNo) {
        // 1. 그룹 내 일일학습 목록 조회
        List<TeacherDailyStudyResponse> studies  = groupStudyMapper.getTeacherDailyStudiesByGroupNo(groupNo);

        // 2. 각 학습자료의 부가 통계 계산
        for (TeacherDailyStudyResponse study : studies) {
            int completedCount = groupStudyMapper.countCompletedStudents(study.getDailyStudyNo());
            int essayCompletedCount = groupStudyMapper.countEssayCompletedStudents(study.getDailyStudyNo());

            study.setCompletedStudentCount(completedCount);
            study.setEssayCompletedStudentCount(essayCompletedCount);
        }

        return studies;
    }

    /**
     * 그룹 학습자료 목록 조회 서비스
     * 그룹에 업로드된 학습자료는 이미 AI 재구성 완료 상태이므로
     * 단순히 DB에서 조회 후 DTO 변환하여 반환한다.
     */
    public List<DailyStudySummaryResponse> getGroupDailyStudies(User user, int rows, int groupNo) {
        log.info("getGroupDailyStudies() 실행 - groupNo={}, rows={}", groupNo, rows);

        Map<String, Object> dailyStudyCondition = new HashMap<>();
        dailyStudyCondition.put("rows", rows);

        // Mapper 호출 (단순 SELECT)
        List<UserStudyRecommendationDto> groupStudies = dailyStudyMapper.getDailyStudiessByGroupAndUser(
            groupNo,
            user.getUserNo(),
            dailyStudyCondition);

        // DTO 변환
        return groupStudies.stream()
            .map(studyService::mapToDailyStudySummaryResponse)
            .collect(Collectors.toList());
    }
}
