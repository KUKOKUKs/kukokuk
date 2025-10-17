package com.kukokuk.integration.redis;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.study.service.GroupStudyService;
import com.kukokuk.domain.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/worker/callback")
@RequiredArgsConstructor
@Log4j2
public class ApiWorkerCallbackController {

    private final GroupStudyService groupStudyService;
    private final StudyService studyService;

    @PostMapping("/materials")
    public ResponseEntity<ApiResponse<Void>> handleMaterialCallback(@RequestBody WorkerMaterialCallbackRequest request) {
        log.info("handleMaterialCallback 파이썬 워커 콜백함수 실행");

        // 추출된 텍스트를 DB에 저장, difficulty가 있을 시, LLMAI 호출로 재가공 자료 생성 및 DB에 저장, jobid의 작업 갱신
        groupStudyService.handleWorkerCallback(request);
        
        return ResponseEntityUtils.ok("추출된 텍스트 저장 및 AI 재가공 완료");
    }

    @PostMapping("/materials/admin")
    public ResponseEntity<ApiResponse<Void>> handleAdminMaterialCallback(@RequestBody WorkerMaterialCallbackRequest request) {
        log.info("handleAdminMaterialCallback 파이썬 워커 콜백함수 실행");

        // 추출된 텍스트를 DB에 저장, jobid의 작업 갱신
        studyService.handleAdminWorkerCallback2(request);

        return ResponseEntityUtils.ok("추출된 텍스트 저장 및 AI 재가공 완료");
    }
}
