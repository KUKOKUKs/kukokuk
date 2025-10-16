package com.kukokuk.domain.edunet;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
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
public class EdunetWorkerCallbackController {

    private final EdunetUrlParseService edunetUrlParseService;


    /**
     * 관리자 학습자료 파싱 성공 콜백
     *
     * 파이썬 워커가 Redis 큐(parse:queue:admin) 작업을 완료한 뒤
     * Spring 서버로 결과를 전송하는 엔드포인트
     *
     * @param request 워커에서 전달한 학습자료 파싱 결과(JSON)
     * @return 처리 성공 응답
     */
    @PostMapping("/materials/admin/edunet")
    public ResponseEntity<ApiResponse<Void>> handleAdminMaterialCallback(@RequestBody WorkerAdminCallbackRequest request) {
        log.info("handleAdminMaterialCallback 파이썬 워커 콜백함수 실행");

        edunetUrlParseService.handleAdminWorkerCallback(request);

        return ResponseEntityUtils.ok("관리자 학습자료 파싱 완료");
    }

    /**
     * 관리자 학습자료 파싱 실패 콜백
     *
     * 파이썬 워커에서 예외 발생 시 호출되는 엔드포인트
     *
     * @param request 실패한 jobNo 및 에러 메시지를 포함한 요청 객체
     * @return 처리 완료 응답
     */
    @PostMapping("/admin/edunet/fail")
    public ResponseEntity<ApiResponse<Void>> handleAdminMaterialParseFailCallback(
        @RequestBody WorkerAdminCallbackRequest request) {

        edunetUrlParseService.handleAdminWorkerFailCallback(request);

        return ResponseEntityUtils.ok("실패 상태 반영 완료");
    }
}
