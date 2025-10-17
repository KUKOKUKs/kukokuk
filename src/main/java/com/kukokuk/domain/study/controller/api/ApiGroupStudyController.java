package com.kukokuk.domain.study.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.study.dto.DailyStudySummaryResponse;
import com.kukokuk.domain.study.service.GroupStudyService;
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
@RequestMapping("/api/groups/{groupNo}")
@RequiredArgsConstructor
public class ApiGroupStudyController {

    private final GroupStudyService groupStudyService;

    /**
     * GET /api/groups/{groupNo}/studies?rows=
     * 그룹에 업로드된 완성된 학습자료(DailyStudy) 목록을 반환
     * 사용자 로그인이 필수이며, 사용자의 이력을 기반으로 목록을 조회한다
     */
    @GetMapping("/studies")
    public ResponseEntity<ApiResponse<List<DailyStudySummaryResponse>>> getGroupDailyStudies(
        @PathVariable int groupNo,
        @RequestParam(defaultValue = "5") int rows,
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        log.info("그룹 학습자료 목록 조회 컨트롤러 실행 groupNo={}, rows={}", groupNo, rows);

        List<DailyStudySummaryResponse> studies = groupStudyService.getGroupDailyStudies(securityUser.getUser(), rows, groupNo);

        return ResponseEntityUtils.ok("그룹 학습자료 목록 조회 성공",studies);
    }
}
