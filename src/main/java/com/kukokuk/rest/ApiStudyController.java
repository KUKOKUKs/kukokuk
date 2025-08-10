package com.kukokuk.rest;

import com.kukokuk.dto.UserStudyRecommendationDto;
import com.kukokuk.request.CreateStudyLogRequest;
import com.kukokuk.request.ParseMaterialRequest;
import com.kukokuk.request.StudyQuizLogRequest;
import com.kukokuk.request.UpdateStudyLogRequest;
import com.kukokuk.response.ApiResponse;
import com.kukokuk.response.DailyStudyLogResponse;
import com.kukokuk.response.DailyStudySummaryResponse;
import com.kukokuk.response.ParseMaterialResponse;
import com.kukokuk.response.ResponseEntityUtils;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.StudyService;
import com.kukokuk.vo.DailyStudy;
import com.kukokuk.vo.DailyStudyLog;
import com.kukokuk.vo.DailyStudyMaterial;
import com.kukokuk.vo.DailyStudyQuizLog;
import com.kukokuk.vo.StudyDifficulty;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class ApiStudyController {

    private final StudyService studyService;

    /**
     * POST /api/studies/parse-materials
     * 에듀넷 경로를 전달하면 비동기 큐로 파싱작업을 수행한 후 원본데이터를 DB에 저장하는 API
     * 요청 바디 : { urls : [에듀넷 url 경로 리스트]}
     * 응답 바디 : { skippedUrls : 스킵된 작업(중복 url) , enqueuedUrls : 큐에 저장되어 진행예정인 작업}
     */
    @PostMapping("/parse-materials")
    public ResponseEntity<ApiResponse<ParseMaterialResponse>> parseMaterialByEdunetUrl(@RequestBody
    ParseMaterialRequest request) {

        // 파이썬 서버를 호출하는 서비스 메소드 호출
        ParseMaterialResponse parseMaterialResponse = studyService.createMaterial(request);

        // 전달받은 응답데이터를 응답통일 객체의 data부분에 설정
        ApiResponse<ParseMaterialResponse> apiResponse = ApiResponse.success(parseMaterialResponse);

        return ResponseEntity
            .ok()
            .body(apiResponse);
    }

    /**
     * POST /api/studies 아직 사용할 일 없어서 추후 수정 예정
     */
    @PostMapping()
    public ResponseEntity<ApiResponse> createStudy() {

        studyService.createDailyStudy(3, 1);

        return ResponseEntity
            .ok()
            .body(null);
    }

    /**
     * GET /api/studies?rows=
     * 사용자의 수준, 진도에 맞는 학습자료 목록을 제공하는 API
     * 응답 바디 : { "dailyStudyNo": 1,
     *              "title": "문단 배우기: 중심 문장과 뒷받침 문장",
     *              "explanation" : "학습 메인설명",
     *              "cardCount" : 3, // 일일학습의 총 카드개수
     *              "status" : "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
     *              "studiedCardCount": 2, // 해당 사용자가 이 일일학습에서 학습한 카드 개수
     *              "progressRate" : 66,
     *              "school" : "초등", // "초등", "중등",
     *              grade" : 1,
     *              "sequence" : 3 // 학년 내 자료의 순서
     *          }
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DailyStudySummaryResponse>>> getStudiesByUser(
        @RequestParam(defaultValue = "5") int rows,
        @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("getStudiesByUser 컨트롤러 실헹");

        // 사용자의 수준과 진도에 맞는 추천 학습자료(DailyStudy) 목록을 조회하는 메소드 호출
        List<UserStudyRecommendationDto> dtos = studyService.getUserDailyStudies(
            securityUser.getUser(), rows);

        // UserStudyRecommendationDto에서 응답에 필요한 정보만 반환하도록 ResponseDTO에 매핑
        List<DailyStudySummaryResponse> responses = dtos.stream()
            .map(dto -> {
                DailyStudy study = dto.getDailyStudy();
                DailyStudyLog log = dto.getDailyStudyLog();
                DailyStudyMaterial material = dto.getDailyStudyMaterial();

                int totalCardCount = study.getCardCount();
                int studiedCardCount = (log != null && log.getStudiedCardCount() != null) ? log.getStudiedCardCount() : 0;
                int progressRate =
                    (totalCardCount == 0) ? 0 : (int) ((studiedCardCount * 100.0) / totalCardCount);

                String status = "NOT_STARTED";
                if (log != null) {
                    status = log.getStatus(); // "IN_PROGRESS", "COMPLETED" 중 하나라고 가정
                }

                return DailyStudySummaryResponse.builder()
                    .dailyStudyNo(study.getDailyStudyNo())
                    .title(study.getTitle())
                    .explanation((study.getExplanation()))
                    .cardCount(totalCardCount)
                    .status(status)
                    .studiedCardCount(studiedCardCount)
                    .progressRate(progressRate)
                    .school(material.getSchool())
                    .grade(material.getGrade())
                    .sequence(material.getSequence())
                    .build();
            })
            .toList();

        return ResponseEntityUtils.ok("사용자 맞춤 학습자료 목록 조회 성공", responses);
    }

    /**
     * POST /api/studies/logs
     * 사용자의 학습자료에 대한 학습이력 생성
     * 요청 바디 : { dailyStudyNo : 학습자료번호 }
     */
    @PostMapping("/logs")
    public ResponseEntity<ApiResponse<DailyStudyLog>> createDailyStudyLog(
        @RequestBody CreateStudyLogRequest createStudyLogRequest,
        @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("createDailyStudyLog 컨트롤러 실행");

        DailyStudyLog log = studyService.createDailyStudyLog(
            createStudyLogRequest.getDailyStudyNo(), securityUser.getUser().getUserNo());

        return ResponseEntityUtils.ok("사용자 학습 이력 생성 성공", log);
    }

    /**
     * PUT /api/studies/logs/{dailyStudyLogNo}
     * 학습 이력 수정
     * 요청 바디 : { studiedCardCount : 학습카드개수, status: 학습 상태 }
     */
    @PutMapping("/logs/{dailyStudyLogNo}")
    public ResponseEntity<ApiResponse<DailyStudyLogResponse>> updateDailyStudyLog(
        @RequestBody UpdateStudyLogRequest updateStudyLogRequest,
        @PathVariable("dailyStudyLogNo") int dailyStudyLogNo,
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        log.info("updateDailyStudyLog 컨트롤러 실행");

        DailyStudyLogResponse response = studyService.updateDailyStudyLog(dailyStudyLogNo,
            updateStudyLogRequest, securityUser.getUser().getUserNo());

        return ResponseEntityUtils.ok("사용자 학습 이력 수정 성공", response);
    }

    /**
     * POST /api/studies/quizzes/logs
     * 사용자의 학습퀴즈 이력 생성
     * 요청 바디 : { dailyStudyQuizNo : 학습퀴즈번호. selectedChoice : 사용자가 선택한 보기 }
     */
    @PostMapping("/quizzes/logs")
    public ResponseEntity<ApiResponse<DailyStudyQuizLog>> createStudyQuizLog(
        @RequestBody StudyQuizLogRequest studyQuizLogRequest,
        @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("createStudyQuizLog 컨트롤러 실행");

        DailyStudyQuizLog log = studyService.createStudyQuizLog(studyQuizLogRequest,
            securityUser.getUser().getUserNo());

        return ResponseEntityUtils.ok("사용자 퀴즈 이력 생성 성공", log);
    }

    /**
     * PUT /api/studies/logs/{dailyStudyLogNo}
     * 학습 이력 수정
     * 요청 바디 : { studiedCardCount : 학습카드개수, status: 학습 상태 }
     */
    @PutMapping("/quizzes/logs/{studyQuizLogNo}")
    public ResponseEntity<ApiResponse<DailyStudyQuizLog>> updateStudyQuizLog(
        @RequestBody StudyQuizLogRequest studyQuizLogRequest,
        @PathVariable("studyQuizLogNo") int studyQuizLogNo,
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        log.info("updateStudyQuizLog 컨트롤러 실행");

        DailyStudyQuizLog updatedlog = studyService.updateStudyQuizLog(studyQuizLogNo,
            studyQuizLogRequest, securityUser.getUser().getUserNo());

        return ResponseEntityUtils.ok("사용자 학습퀴즈 이력 수정 성공", updatedlog);
    }

    @GetMapping("/difficulties")
    public ResponseEntity<ApiResponse<List<StudyDifficulty>>> getStudyDifficulties() {
        List<StudyDifficulty> studyDifficulties = studyService.getStudyDifficulties();
        return ResponseEntityUtils.ok("학습수준 목록 조회 성공", studyDifficulties);
    }
}
