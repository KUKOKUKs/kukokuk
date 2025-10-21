package com.kukokuk.domain.study.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.study.dto.ParseMaterialRequest;
import com.kukokuk.domain.study.service.StudyService;
import com.kukokuk.domain.study.dto.ParseMaterialResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/studies")
@RequiredArgsConstructor
public class ApiAdminStudyController {

    private final StudyService studyService;

    /**
    * 관리자가 hwp/hwpx 파일을 업로드하면,
    * @param files
    * @return
    */
    @PostMapping("/materials/upload")
    public ResponseEntity<ApiResponse<List<String>>> uploadGroupMaterials(
        @RequestParam("files") List<MultipartFile> files, // 일반적으로 파일은 RequestParam으로 받음
        @RequestParam("school") String school,
        @RequestParam("grade") int grade
    ) {
        // 그룹의 학습자료 업로드 요청을 처리하는 서비스. jobStatus를 생성하고, Redis 큐에 작업 적재
        List<String> jobIdList = studyService.uploadAdminMaterials(files, school, grade);

        return ResponseEntityUtils.ok("관리자 자료 업로드 성공",jobIdList);
    }
}
