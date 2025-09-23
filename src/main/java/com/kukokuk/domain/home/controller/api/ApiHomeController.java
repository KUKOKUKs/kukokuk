package com.kukokuk.domain.home.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.dto.JobStatusResponse;
import com.kukokuk.common.exception.BadRequestException;
import com.kukokuk.common.store.RedisJobStatusStore;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.home.service.HomeService;
import com.kukokuk.domain.study.dto.DailyStudySummaryResponse;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class ApiHomeController {

    private final HomeService homeService;
    private final RedisJobStatusStore<List<DailyStudySummaryResponse>> studyJobStatusStore;

    /*
    !! 정호형님 확인!!!
        study 컨트롤러에 통일됨에 따라 확인후 삭제 - /api/studies
     */
    // 맞춤 학습 자료 요청
    /*
    @GetMapping("/studies")
    public ResponseEntity<ApiResponse<JobStatusResponse<?>>> getStudiesByUser(
        @RequestParam(defaultValue = "1") int dailyStudyCount
        , @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("ApiHomeController getStudiesByUser() 컨트롤러 실헹");

        // 조건별 jobId 생성
        // 맞춤학습에 관련된 유니크 키설정
        // userNo, school, grade, difficulty
        String jobId = String.format("userNo:%d:school:%s:grade:%d:difficulty:%d"
            , securityUser.getUser().getUserNo()
            , securityUser.getUser().getCurrentSchool()
            , securityUser.getUser().getCurrentGrade()
            , securityUser.getUser().getStudyDifficulty());
        log.info("ApiHomeController getStudiesByUser() jobId: {}", jobId);

        // 기존 Job 확인
        JobStatusResponse<?> existingStatus = studyJobStatusStore.get(jobId);
        log.info("existingStatus: {}", existingStatus);

        // 중복된 요청 즉 새로고침이나 재요청이 발생된 경우
        // PROCESSING 상태일 경우에만 반환
        if (existingStatus != null && "PROCESSING".equals(existingStatus.getStatus())) {
            return ResponseEntityUtils.ok(existingStatus);
        }

        // 그 외(DONE, FAILED) -> Redis에서 제거 후 새 작업 생성
        // DONE일 경우 DB에 저장된 데이터 반환 후 또는 백그라운드 작업 완료로
        // 현재 사용자의 상태의 맞춤 학습자료를 서비스 메소드 호출 시 생성하지 않고 DB데이터를 제공할 수 있음
        // FAILED일 경우 생성/저장에 실패하여 다시 시도
        studyJobStatusStore.delete(jobId);

        // 초기 상태 생성
        JobStatusResponse<List<DailyStudySummaryResponse>> status =
            JobStatusResponse.<List<DailyStudySummaryResponse>>builder()
            .jobId(jobId)
            .status("PROCESSING")
            .progress(0)
            .result(null)
            .message("맞춤 학습 자료 요청 중...")
            .build();
        log.info("status: {}", status);

        // Redis에 저장
        studyJobStatusStore.put(status);
        
        // 백그라운드 비동기 처리 로직 추가
        //homeService.getHomeUserDailyStudies(jobId, securityUser.getUser(), dailyStudyCount);

        return ResponseEntityUtils.ok(status);
    }
     */

    /*
        !! 정호형님 확인 !!
        - apiStudyController로 이동
     */
    // 맞춤 학습 자료 폴링(최초 요청시 응답 받은 jobId)
    /*
    @GetMapping("/studies/status/{jobId}")
    public ResponseEntity<ApiResponse<JobStatusResponse<?>>> getStudiesByUserStatus(
        @PathVariable("jobId") String jobId) {
        log.info("ApiHomeController getStudiesByUserStatus() 컨트롤러 실헹");

        // 상태 조회
        JobStatusResponse<?> status = studyJobStatusStore.get(jobId);

        if (status == null) {
            throw new BadRequestException(jobId + "의 상태를 찾을 수 없습니다.");
        }

        return ResponseEntityUtils.ok(status);
    }

     */

}
