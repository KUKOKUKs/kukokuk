package com.kukokuk.domain.study.controller.api;

import com.kukokuk.common.constant.PaginationEnum;
import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.dto.JobStatusResponse;
import com.kukokuk.common.dto.Page;
import com.kukokuk.common.exception.BadRequestException;
import com.kukokuk.common.store.JobStatusStore;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.study.dto.DailyStudyLogDetailResponse;
import com.kukokuk.domain.study.dto.DailyStudySummaryResponse;
import com.kukokuk.domain.study.dto.EssayQuizLogRequest;
import com.kukokuk.domain.study.dto.EssayQuizLogResponse;
import com.kukokuk.domain.study.dto.GeminiEssayResponse;
import com.kukokuk.domain.study.service.StudyAsyncService;
import com.kukokuk.domain.study.service.StudyService;
import com.kukokuk.domain.study.vo.DailyStudyEssayQuizLog;
import com.kukokuk.domain.study.vo.StudyDifficulty;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
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
    private final StudyAsyncService studyAsyncService;
    private final ModelMapper modelMapper;
    private final JobStatusStore<DailyStudySummaryResponse> studyJobStatusStore;

    /**
     * POST /api/studies 아직 사용할 일 없어서 추후 수정 예정
     */
    @PostMapping()
    public ResponseEntity<ApiResponse> createStudy() {

        studyService.createDailyStudyByAi(3, 1);

        return ResponseEntity
            .ok()
            .body(null);
    }

    /**
     * GET /api/studies?rows=
     * 사용자의 수준, 진도에 맞는 학습자료 목록을 제공하는 API
     * 응답으로 JobStatusResponse<DailyStudySummaryResponse>의 목록이 반환됨
     *
     * 응답 바디 : [{ "dailyStudyNo": 1,
     *              "title": "문단 배우기: 중심 문장과 뒷받침 문장",
     *              "explanation" : "학습 메인설명",
     *              "cardCount" : 3, // 일일학습의 총 카드개수
     *              "status" : "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
     *              "studiedCardCount": 2, // 해당 사용자가 이 일일학습에서 학습한 카드 개수
     *              "progressRate" : 66,
     *              "school" : "초등", // "초등", "중등",
     *              grade" : 1,
     *              "sequence" : 3 // 학년 내 자료의 순서
     *          }]
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<JobStatusResponse<DailyStudySummaryResponse>>>> getStudiesByUser(
        @RequestParam(defaultValue = "5") int rows,
        @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("ApiStudyController getStudiesByUser() 컨트롤러 실헹");

        List<JobStatusResponse<DailyStudySummaryResponse>> responses = studyAsyncService.getUserDailyStudies(
            securityUser.getUser(), rows);

        return ResponseEntityUtils.ok(responses);
    }

    /**
     * POST /api/studies/logs
     * 사용자의 학습자료에 대한 학습이력 생성
     * 요청 바디 : { dailyStudyNo : 학습자료번호 }
     */
//    @PostMapping("/logs")
//    public ResponseEntity<ApiResponse<DailyStudyLog>> createDailyStudyLog(
//        @RequestBody CreateStudyLogRequest createStudyLogRequest,
//        @AuthenticationPrincipal SecurityUser securityUser) {
//        log.info("createDailyStudyLog 컨트롤러 실행");
//
//        DailyStudyLog log = studyService.createDailyStudyLog(
//            createStudyLogRequest.getDailyStudyNo(), securityUser.getUser().getUserNo());
//
//        return ResponseEntityUtils.ok("사용자 학습 이력 생성 성공", log);
//    }

    /**
     * PUT /api/studies/logs/{dailyStudyLogNo}
     * 학습 이력 수정
     * 요청 바디 : { studiedCardCount : 학습카드개수, status: 학습 상태 }
     */
//    @PutMapping("/logs/{dailyStudyLogNo}")
//    public ResponseEntity<ApiResponse<DailyStudyLogResponse>> updateDailyStudyLog(
//        @RequestBody UpdateStudyLogRequest updateStudyLogRequest,
//        @PathVariable("dailyStudyLogNo") int dailyStudyLogNo,
//        @AuthenticationPrincipal SecurityUser securityUser
//    ) {
//        log.info("updateDailyStudyLog 컨트롤러 실행");
//
//        DailyStudyLogResponse response = studyService.updateDailyStudyLog(dailyStudyLogNo,
//            updateStudyLogRequest, securityUser.getUser().getUserNo());
//
//        return ResponseEntityUtils.ok("사용자 학습 이력 수정 성공", response);
//    }

    /**
     * POST /api/studies/quizzes/logs
     * 사용자의 학습퀴즈 이력 생성
     * 요청 바디 : { dailyStudyQuizNo : 학습퀴즈번호. selectedChoice : 사용자가 선택한 보기 }
     */
//    @PostMapping("/quizzes/logs")
//    public ResponseEntity<ApiResponse<DailyStudyQuizLog>> createStudyQuizLog(
//        @RequestBody StudyQuizLogRequest studyQuizLogRequest,
//        @AuthenticationPrincipal SecurityUser securityUser) {
//        log.info("createStudyQuizLog 컨트롤러 실행");
//
//        DailyStudyQuizLog log = studyService.createStudyQuizLog(studyQuizLogRequest,
//            securityUser.getUser().getUserNo());
//
//        return ResponseEntityUtils.ok("사용자 퀴즈 이력 생성 성공", log);
//    }

    /**
     * PUT /api/studies/logs/{dailyStudyLogNo}
     * 학습 이력 수정
     * 요청 바디 : { studiedCardCount : 학습카드개수, status: 학습 상태 }
     */
//    @PutMapping("/quizzes/logs/{studyQuizLogNo}")
//    public ResponseEntity<ApiResponse<DailyStudyQuizLog>> updateStudyQuizLog(
//        @RequestBody StudyQuizLogRequest studyQuizLogRequest,
//        @PathVariable("studyQuizLogNo") int studyQuizLogNo,
//        @AuthenticationPrincipal SecurityUser securityUser
//    ) {
//        log.info("updateStudyQuizLog 컨트롤러 실행");
//
//        DailyStudyQuizLog updatedlog = studyService.updateStudyQuizLog(studyQuizLogNo,
//            studyQuizLogRequest, securityUser.getUser().getUserNo());
//
//        return ResponseEntityUtils.ok("사용자 학습퀴즈 이력 수정 성공", updatedlog);
//    }

    @GetMapping("/difficulties")
    public ResponseEntity<ApiResponse<List<StudyDifficulty>>> getStudyDifficulties() {
        List<StudyDifficulty> studyDifficulties = studyService.getStudyDifficulties();
        return ResponseEntityUtils.ok("학습수준 목록 조회 성공", studyDifficulties);
    }

    /**
     * POST /api/studies/essays/logs
     * 서술형 퀴즈의 이력을 생성
     * 요청바디 : { "dailyStudyEssayQuizNo": 101,
     *   "userAnswer": "유저가 작성한 답변" }
     */
    @PostMapping("/essays/logs")
    public ResponseEntity<ApiResponse<EssayQuizLogResponse>> createEssayQuizLog(@RequestBody EssayQuizLogRequest essayQuizLogRequest,
        @AuthenticationPrincipal SecurityUser securityUser) {

        DailyStudyEssayQuizLog essayQuizLog = studyService.createStudyEssayQuizLog(
            essayQuizLogRequest, securityUser.getUser().getUserNo());

        EssayQuizLogResponse response = modelMapper.map(essayQuizLog, EssayQuizLogResponse.class);

        return ResponseEntityUtils.ok("서술형 퀴즈 이력 생성 성공", response);
    }

    /**
     * PUT /api/studies/essays/logs/{essayQuizLogNo}
     * 서술형 퀴즈의 이력을 수정
     * 요청바디 : { "dailyStudyEssayQuizNo": 101,
     *   "userAnswer": "유저가 작성한 답변" }
     */
    @PutMapping("/essays/logs/{essayQuizLogNo}")
    public ResponseEntity<ApiResponse<EssayQuizLogResponse>> updateEssayQuizLog(
        @PathVariable("essayQuizLogNo") int essayQuizLogNo,
        @RequestBody EssayQuizLogRequest essayQuizLogRequest,
        @AuthenticationPrincipal SecurityUser securityUser) {

        DailyStudyEssayQuizLog essayQuizLog = studyService.updateStudyEssayQuizLog(essayQuizLogNo,
            essayQuizLogRequest, securityUser.getUser().getUserNo());

        EssayQuizLogResponse response = modelMapper.map(essayQuizLog, EssayQuizLogResponse.class);

        return ResponseEntityUtils.ok("서술형 퀴즈 이력 수정 성공", response);
    }

    /**
     * PUT /api/studies/essays/ai
     * ai 피드백 생성 및 DB에 저장
     * 요청바디 : {
     * 	"dailyStudyEssayQuizLogNo" : 1 // 사용자 이력이 이미존재하면 포함하여 보냄
     *   "dailyStudyEssayQuizNo": 101,
     *   "userAnswer": "유저가 작성한 답변"
     * }
     */
    @PutMapping("/essays/ai")
    public ResponseEntity<ApiResponse<GeminiEssayResponse>> updateAiFeedback(
        @RequestBody EssayQuizLogRequest request,
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        log.info("updateAiFeedback 컨트롤러 실행");

        int userNo = securityUser.getUser().getUserNo();
        DailyStudyEssayQuizLog essayQuizLog = null;

        // 요청 본문에 essayQuizLogNo 필드가 존재한다면, 기존 이력 업데이트
        if (request.getDailyStudyEssayQuizLogNo() != null) {
            essayQuizLog = studyService.updateStudyEssayQuizLog(request.getDailyStudyEssayQuizLogNo(), request, userNo);
        }
        // 기존 이력 존재하지 않으면 생성
        else {
            essayQuizLog = studyService.createStudyEssayQuizLog(request, userNo);
        }

        // AI 피드백 생성하는 서비스 메소드 호출
        GeminiEssayResponse response = studyService.generateAiFeedback(essayQuizLog, securityUser.getUser().getStudyDifficulty());

        return ResponseEntityUtils.ok("AI 피드백 생성 및 저장 완료", response);
    }

    // 맞춤 학습 자료 폴링(최초 요청시 응답 받은 jobId)
    // 왜 여기서 jobId로 JobStatusResponse을 가져오지 못할까..?
    @GetMapping("/status/{jobId}")
    public ResponseEntity<ApiResponse<JobStatusResponse<?>>> getStudiesByUserStatus(
        @PathVariable("jobId") String jobId) {
        log.info("ApiStudyController getStudiesByUserStatus() 컨트롤러 실헹 jobId: {}", jobId);

        // 상태 조회
        JobStatusResponse<?> status = studyJobStatusStore.get(jobId);
        log.info("status: {}", status);

        if (status == null) {
            throw new BadRequestException(jobId + "의 상태를 찾을 수 없습니다.");
        }

        return ResponseEntityUtils.ok(status);
    }

    /**
     * [GET] /api/studies/logs
     *
     * 학습 히스토리 화면에서 사용자별 학습 이력(상세 정보 포함)을 조회
     *
     * 요청 파라미터:
     *  - page : 현재 페이지 번호 (기본값 1)
     *  - rows : 한 페이지당 행 수 (기본값 10)
     *
     * 응답 구조:
     * {
     *   "success": true,
     *   "status": 200,
     *   "message": "학습 이력 목록 조회 성공",
     *   "data": [
     *     {
     *       "dailyStudyLogNo": 1,
     *       "dailyStudyNo": 23,
     *       "dailyStudyTitle": "속담 익히기 1일차",
     *       "status": "COMPLETED",
     *       "startedDate": "2025-10-09T13:20:00",
     *       "updatedDate": "2025-10-09T14:10:00",
     *       "totalCardCount": 5,
     *       "completedCardCount": 5,
     *       "totalQuizCount": 3,
     *       "successedQuizCount": 2,
     *       "essaySubmitted": true,
     *       "difficulty": 3
     *     }
     *   ]
     * }
     *
     * @param securityUser 현재 로그인한 사용자
     * @param page 페이지 번호 (기본값 1)
     * @param rows 한 페이지당 행 수 (기본값 10)
     * @return 학습 이력 목록을 포함한 표준 ApiResponse
     */
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Page<DailyStudyLogDetailResponse>>> getStudyLogs(
        @AuthenticationPrincipal SecurityUser securityUser
        , @RequestParam(defaultValue = "1") int page
        , @RequestParam(required = false) Integer rows) {
        log.info("ApiStudyController getStudyLogs() 컨트롤러 실헹");

        // 조회할 행의 수를 입력하지 않았을 경우 기본 값 10
        if (rows == null) {
            rows = PaginationEnum.DEFAULT_ROWS;
        }

        return ResponseEntityUtils.ok(
            "학습 이력 목록 조회 성공"
            , studyService.getStudyLogsDetail(securityUser.getUser().getUserNo(), page, rows)
        );
    }
}
