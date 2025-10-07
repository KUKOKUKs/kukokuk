package com.kukokuk.domain.study.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.dto.JobStatusResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.study.dto.GroupParseMaterialResponse;
import com.kukokuk.domain.study.dto.TeacherDailyStudyResponse;
import com.kukokuk.domain.study.service.GroupStudyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/teachers/groups/{groupNo}")
@RequiredArgsConstructor
public class ApiTeacherGroupController {

    private final GroupStudyService groupStudyService;

    /**
     * 교사가 hwp/hwpx 파일을 업로드하면,
     * @param files
     * @param groupNo
     * @return
     */
    @PostMapping("/materials/upload")
    public ResponseEntity<ApiResponse<List<String>>> uploadGroupMaterials(
        @RequestParam("files") List<MultipartFile> files, // 일반적으로 파일은 RequestParam으로 받음
        @PathVariable("groupNo") int groupNo,
        @RequestParam("difficulty") int difficulty
    ) {

        // 그룹의 학습자료 업로드 요청을 처리하는 서비스. jobStatus를 생성하고, Redis 큐에 작업 적재
        List<String> jobIdList = groupStudyService.uploadGroupMaterials(files, groupNo, difficulty);

        return ResponseEntityUtils.ok(jobIdList);
    }

    // 그룹 전체의 진행중인 job 목록 조회
    @GetMapping("/materials/jobs")
    public ResponseEntity<List<JobStatusResponse<GroupParseMaterialResponse>>> getGroupJobs(
        @PathVariable int groupNo
    ) {
        List<JobStatusResponse<GroupParseMaterialResponse>> jobs =
            groupStudyService.getAllJobsByGroup(groupNo);
        return ResponseEntity.ok(jobs);
    }

    // 특정 jobId 상태 조회 (폴링용)
    @GetMapping("/materials/{jobId}")
    public ResponseEntity<JobStatusResponse<GroupParseMaterialResponse>> getJobStatus(
        @PathVariable int groupNo,
        @PathVariable String jobId
    ) {
        JobStatusResponse<GroupParseMaterialResponse> jobStatus =
            groupStudyService.getJobStatus(jobId);
        return ResponseEntity.ok(jobStatus);
    }

    // 특정 그룹의 일일학습자료 목록 조회
    @GetMapping("/studies")
    public ResponseEntity<ApiResponse<List<TeacherDailyStudyResponse>>> getGroupDailyStudies(
        @PathVariable int groupNo) {
        List<TeacherDailyStudyResponse> dailyStudies = groupStudyService.getGroupDailyStudies(groupNo);
        return ResponseEntityUtils.ok(dailyStudies);
    }
}
