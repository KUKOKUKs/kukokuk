package com.kukokuk.domain.study.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.dto.JobStatusResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.study.dto.GroupParseMaterialResponse;
import com.kukokuk.domain.study.service.GroupStudyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/groups/{groupId}")
@RequiredArgsConstructor
public class ApiGroupStudyController {

    private final GroupStudyService groupStudyService;

    /**
     * 교사가 hwp/hwpx 파일을 업로드하면,
     * @param files
     * @param groupId
     * @return
     */
    @PostMapping("/materials/upload")
    public ResponseEntity<ApiResponse<List<JobStatusResponse<GroupParseMaterialResponse>>>> uploadGroupMaterials(
        @RequestParam("files") List<MultipartFile> files, // 일반적으로 파일은 RequestParam으로 받음
        @PathVariable("groupId") int groupId,
        @RequestParam("difficulty") int difficulty
    ) {

        groupStudyService.uploadGroupMaterials(files, groupId, difficulty);

        List<JobStatusResponse<GroupParseMaterialResponse>> jobStatusResponses = null;

        return ResponseEntityUtils.ok(jobStatusResponses);
    }

    @GetMapping("/materials")
    public ResponseEntity<ApiResponse<List<JobStatusResponse<GroupParseMaterialResponse>>>> getGroupMaterials() {
        List<JobStatusResponse<GroupParseMaterialResponse>> jobStatusResponses = null;
        return ResponseEntityUtils.ok(jobStatusResponses);
    }
}
