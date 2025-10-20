package com.kukokuk.domain.study.service;

import com.kukokuk.common.dto.JobStatusResponse;
import com.kukokuk.common.store.JobStatusStore;
import com.kukokuk.common.util.SchoolGradeUtils;
import com.kukokuk.domain.study.dto.DailyStudyJobPayload;
import com.kukokuk.domain.study.dto.DailyStudySummaryResponse;
import com.kukokuk.domain.study.dto.UserStudyRecommendationDto;
import com.kukokuk.domain.study.mapper.DailyStudyMapper;
import com.kukokuk.domain.user.vo.User;
import com.kukokuk.integration.redis.DailyStudyWorker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.internal.Pair;
import org.springframework.stereotype.Service;

/**
 * 비동기 학습자료 생성 요청을 담당하는 서비스 계층
 *
 * [존재 이유]
 * - 원래 getUserDailyStudies 메서드는 StudyService 안에 있었고,
 *   내부에서 DailyStudyWorker(@Async) 를 호출하도록 구현되어 있었음
 * - 그러나 DailyStudyWorker 또한 StudyService를 주입받아 사용하는 구조라
 *   StudyService ↔ DailyStudyWorker 간 순환참조 문제 발생
 *   -> 서버 구동 불가한 문제 발생
 *
 * [해결 방법]
 * - getUserDailyStudies 메서드를 StudyService에서
 *   별도의 StudyAsyncService 클래스로 분리
 * - 이 클래스는 Controller의 진입점에서 직접 호출되며,
 *   내부적으로 DailyStudyWorker를 통해 비동기 작업을 실행
 * - StudyService는 순수한 비즈니스 로직만 담당하고,
 *   비동기 실행 책임은 StudyAsyncService와 Worker가 맡도록 역할을 분리했다.
 *
 * [결과]
 * - 순환참조 문제를 제거하고, 계층 역할을 명확히 분리
 * - 구조: Controller → StudyAsyncService → DailyStudyWorker(@Async) → StudyService
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class StudyAsyncService {

    private final DailyStudyMapper dailyStudyMapper;
    private final JobStatusStore<DailyStudySummaryResponse> studyJobStatusStore;
    private final StudyService studyService;
    private final DailyStudyWorker dailyStudyWorker;


    /**
     * 사용자의 수준과 진도에 맞는 추천 학습자료(DailyStudy) 목록을 조회한다. 최대 recommendStudyCount(기본 5개)까지 추천하며, 필요한 경우
     * GPT 기반으로 학습자료를 생성한다.
     * <p>
     * [전체 처리 단계]
     * <p>
     * 1단계: 사용자 진도(CURRENT_SCHOOL, CURRENT_GRADE)에 해당하는 원본 학습자료를 기준으로 학습자료 + 학습이력과 함께 아우터 조인하여 학습자료
     * DTO 목록을 조회한다. - 조건: 사용자의 수준(STUDY_DIFFICULTY)에 맞는 학습자료 - 조건: 학습이력이 없거나, 학습중(IN_PROGRESS) 상태인
     * 학습자료 - 정렬: 학습중이면 UPDATED_DATE 최신순, 그 외에는 자료순서(SEQUENCE) 순
     * <p>
     * 2단계: 추천 결과가 5개 미만일 경우 → 다음 학년의 학습자료로 부족한 수만큼 추가 조회한다. - 다음 학년이 없을 경우, "진도 종료" 상태로 간주 - 선택적으로
     * 사용자의 currentSchool/currentGrade를 업데이트할 수 있음
     * <p>
     * 3단계: 조회된 DTO 중 GPT 재구성된 학습자료가 없는 경우 → GPT 호출로 학습자료(DailyStudy)를 생성하고 DTO에 설정한다.
     * <p>
     * 4단계: 최종 결과 리스트에서 그 DTO를 반환
     *
     * @param user                user 현재 사용자 정보
     * @param recommendStudyCount recommendStudyCount 반환할 학습자료 수
     * @return 학습자료 목록 (DailyStudy)
     */
    public List<JobStatusResponse<DailyStudySummaryResponse>> getUserDailyStudies(User user, int recommendStudyCount) {
        log.info("getUserDailyStudies 서비스 실행");

        // 1단계 : 현재 사용자 수준/진도 기준으로 학습원본데이터_학습자료_학습이력DTO 목록 조회
      /*
        조회 조건
        1. 사용자 진도(CURRENT_SCHOOL, CURRENT_GRADE)에 해당하는 학습원본데이터 기준으로 학습자료,학습이력을 아우터 조인
        2. 학습자료가 있다면, 사용자 수준(STUDY_DIFFICULTY)에 맞는 학습자료만 포함
        3. 사용자의 학습 이력이 없거나,학습이력이 있어도 학습완료되지 않은 학습자료만 포함
        4. 사용자가 학습중이고, UPDATED_DATE가 최신인 학습자료를 우선 조회
        5. 그 후 학습원본데이터의 자료순서대로 조회 (즉, 사용자 진도의 학습원본데이터 중 사용자가 학습완료하지 않은 원본데이터를 조회)
        6. recommendStudyCount 개수만큼 조회
       */
        Map<String, Object> dailyStudyCondition = new HashMap<>();
        dailyStudyCondition.put("rows", recommendStudyCount);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("studyDifficulty", user.getStudyDifficulty());
        userInfo.put("currentSchool", user.getCurrentSchool());
        userInfo.put("currentGrade", user.getCurrentGrade());

        // 사용자 조건에 맞는 학습원본데이터_학습자료_학습이력DTO 5개 조회
        List<UserStudyRecommendationDto> userStudyRecommendationDtos = dailyStudyMapper.getDailyStudiesByUser(
            user.getUserNo(),
            userInfo,
            dailyStudyCondition
        );

        // 생성되는 DTO 확인 로그
        userStudyRecommendationDtos.forEach(dto -> log.info("사용자 맞춤 study 조회 : {}", dto.toString()));

        String nowCurrentSchool = user.getCurrentSchool();
        Integer nowCurrentGrade = user.getCurrentGrade();


        // 2단계 : 만약 학습원본데이터_학습자료_학습이력DTO가 recommendStudyCount개 미만일 경우, 다음 학년에서 추가조회하여 채워넣기
        while (userStudyRecommendationDtos.size() < recommendStudyCount) {

            // 현재 사용자 학년의 다음 학년 계산 (다음 학년이 없으면 null반환)
            Pair<String, Integer> nextGrade = SchoolGradeUtils.getNextSchoolGrade(
                nowCurrentSchool, nowCurrentGrade);

            // 만약 현재 학년에서 조회된 학습원본데이터_학습자료_학습이력DTO가 0개라면,
            // 사용자의 currentSchool/currenrGrade를 다음 학년으로 변경
            // ** 사용자 진도 변경하는 매퍼 호출 코드 추가 ************

            if(nextGrade == null){
                // 다음학년이 null이면 화면에 이를 식별하는 값을 전달하고,
                // 화면에서 마지막 학년 학습자료라는 표시되고 다른 수준 선택하는 등의 로직 처리
                log.debug("더 이상 다음 학년이 존재하지 않습니다");
                break;
            }

            // 현재 조건이 될 학습진도를 변경
            nowCurrentSchool = nextGrade.getLeft();
            nowCurrentGrade = nextGrade.getRight();

            log.info("다음학년 추가 조회 : {}, {}", nowCurrentSchool, nowCurrentGrade);

            // 더 조회해야할 학습자료 개수 조건 갱신
            int remainingRowCount = recommendStudyCount - userStudyRecommendationDtos.size();
            dailyStudyCondition.put("rows", remainingRowCount);

            // 다음 학년으로 조건 갱신
            userInfo.put("currentSchool", nowCurrentSchool);
            userInfo.put("currentGrade", nowCurrentGrade);

            // 변경된 조건으로 학습원본데이터_학습자료_학습이력DTO 추가 조회
            List<UserStudyRecommendationDto> addUserStudyRecommendationDtos = dailyStudyMapper.getDailyStudiesByUser(
                user.getUserNo(),
                userInfo,
                dailyStudyCondition
            );

            // 기존의 학습원본데이터_학습자료_학습이력DTO 리스트에 추가조회한 DTO 추가
            userStudyRecommendationDtos.addAll(addUserStudyRecommendationDtos);

            // 다음학년을 조회했음에도 채우지못했으면, 개수가 채워질 때 까지 루프가 돈다
        }

        // 응답으로 반환할 작업상태리스트 생성
        List<JobStatusResponse<DailyStudySummaryResponse>> responses = new ArrayList<>();

        // 3단계 : 조회한 학습원본데이터_학습자료_학습이력DTO 리스트 순회
        // 각 DTO에서 학습자료 존재 여부를 확인 후,
        // - 학습자료가 존재하면 status가 DONE인 jobStatus를 응답으로 반환
        // - 학습자료가 존재하지 않으면 jobStatus 생성 및 상태저장소에 저장,
        //      jobPayload 생성 및 Redis 작업 큐에 저장, 별도의 워커(DailyStudyWorker)가 비동기로 학습자료 생성 작업 처리
        for (UserStudyRecommendationDto rec : userStudyRecommendationDtos) {

            // 각 학습자료에 대한 고유한 JobId 생성
            // -> 학습원본자료 번호 + 사용자의 학습 수준 기준으로 멱등키 생성
            String jobId = String.format("material:%d:difficulty:%d"
                , rec.getDailyStudyMaterialNo()
                , user.getStudyDifficulty()
            );

            JobStatusResponse<DailyStudySummaryResponse> status;

            // 학습자료가 아직 생성되지 않은 경우
            if (rec.getDailyStudyNo() == null) {
//                // 해당 학습원본데이터와 사용자수준에 맞는 학습자료 생성하는 메소드 호출
//                DailyStudy newDailyStudy = createDailyStudy(rec.getDailyStudyMaterialNo(),
//                    user.getStudyDifficulty());
//                rec.setDailyStudy(newDailyStudy);

                // 기존 Job 확인
                JobStatusResponse<DailyStudySummaryResponse> existingStatus = studyJobStatusStore.get(jobId);

                // 이미 해당 job이 작업 진행 중인 경우 상태저장소에 새 job을 추가하지 않음 (중복 작업 방지)
                if(existingStatus != null && "PROCESSING".equals(existingStatus.getStatus())) {
                    status = existingStatus;
                } else {
                    // 해당 job이 진행중이지 않은 경우 (DONE, FAILED인 경우도 포함)
                    // 상태저장소에서 기존 작업 (DONE, FAILED인 경우) 제거
                    // 제거 후 다시 생성하는게 워커에서 다시 PROCESSING로 인식해서 재 실행 시키도록 하는건가?
                    studyJobStatusStore.delete(jobId);

                    // Job 상태 객체 생성 (PROCESSING 상태)
                    status = JobStatusResponse.<DailyStudySummaryResponse>builder()
                        .jobId(jobId)
                        .status("PROCESSING")
                        .progress(0)
                        .result(null)
                        .message("맞춤 학습 자료 요청 중...")
                        .build();

                    // 상태저장소(Redis)에 현재 Job 상태 저장
                    studyJobStatusStore.put(status);
                }

                // Worker가 실제 처리할 작업 페이로드 생성
                DailyStudyJobPayload payload = DailyStudyJobPayload.builder()
                    .jobId(jobId)
                    .dailyStudyMaterialNo(rec.getDailyStudyMaterialNo())
                    .studyDifficultyNo(user.getStudyDifficulty())
                    .build();

                // 비동기로 AI 호출 및 DB 저장하는 메소드 호출
                dailyStudyWorker.generateStudyAsync(payload);

            } else {
                // 이미 학습자료가 존재하는 경우
                // DONE 상태의 JobStatusResponse 생성
                // - result에 이미 생성된 학습자료를 담아 반환
                status = JobStatusResponse.<DailyStudySummaryResponse>builder()
                            .jobId(jobId)
                            .status("DONE")
                            .progress(100)
                            .result(studyService.mapToDailyStudySummaryResponse(rec))
                            .message("맞춤 학습 자료가 완성되었습니다.")
                            .build();
            }

            // 응답으로 반환할 job 리스트에 추가
            responses.add(status);
        }

        // 4단계 : 최종 JobStatusResponse 리스트를 컨트롤러로 반환
        return responses;
    }

}
