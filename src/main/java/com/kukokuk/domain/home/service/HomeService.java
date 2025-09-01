package com.kukokuk.domain.home.service;

import com.kukokuk.common.exception.AppException;
import com.kukokuk.common.store.RedisJobStatusStore;
import com.kukokuk.domain.study.dto.DailyStudySummaryResponse;
import com.kukokuk.domain.study.dto.UserStudyRecommendationDto;
import com.kukokuk.domain.study.service.StudyService;
import com.kukokuk.domain.user.vo.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor // 초기화 되지않은 final 필드나, @NonNull 이 붙은 필드에 대해 생성자를 생성
public class HomeService {

    private final StudyService studyService;
    private final RedisJobStatusStore<List<DailyStudySummaryResponse>> studyJobStatusStore;

    /**
     * 비동기 맞춤 학습 자료 가져오기(없을 경우 AI Api 생성 요청)
     * <p>
     *     - 맞춤 학습 자료 요청(studyService 호출)
     *       ㄴ 호출 시 DB에 자료가 있다면 조회하여 반환 없을 경우 AI Api 생성 요청하여
     *          DB에 저장 후 반환
     * </p>
     * - RedisJobStatusStore를 이용해 작업 상태 관리 (PROCESSING, DONE, FAILED)
     * - Object 기반 RedisTemplate 사용
     * - 최초 요청 클라이언트는 PROCESSING 상태 반환
     * - 폴링을 통해 클라이언트에서 완료 상태 확인 가능
     * - @Async 사용으로 비동기 실행, AppException 발생 시 클라이언트는 직접 확인 불가
     *
     * @param jobId Redis에 저장된 Job ID
     * @param user 사용자 정보
     * @param rows 요청할 학습 자료 개수
     */
    @Async("aiTaskExecutor")
    public void getHomeUserDailyStudies(String jobId, User user, int rows) {
        log.info("HomeService getHomeUserDailyStudies() 서비스 실헹");
        // 준비 중
        studyJobStatusStore.update(jobId, status -> {
            status.setProgress(20);
            status.setMessage("학습 이력 확인 중...");
        });

        try {
            // 학습 이력 확인
            studyJobStatusStore.update(jobId, status -> {
                status.setProgress(40);
                status.setMessage("맞춤 학습 자료 생성 중...");
            });

            // 사용자의 수준과 진도에 맞는 추천 학습자료(DailyStudy) 목록을 조회하는 메소드 호출
            List<UserStudyRecommendationDto> dtos = studyService.getUserDailyStudies(
                user, rows);

            // 매핑 중
            studyJobStatusStore.update(jobId, status -> {
                status.setProgress(80);
                status.setMessage("맞춤 학습 자료 정리 중...");
            });

            // UserStudyRecommendationDto에서 응답에 필요한 정보만 반환하도록 ResponseDTO에 매핑
            List<DailyStudySummaryResponse> result =
                studyService.mapToDailyStudySummaryResponse(dtos);

            // 완료
            studyJobStatusStore.update(jobId, status -> {
                status.setResult(result);
                status.setProgress(100);
                status.setStatus("DONE");
                status.setMessage("맞춤 학습 자료가 완성되었습니다.");
            });
        } catch (Exception e) {
            studyJobStatusStore.update(jobId, status -> {
                status.setProgress(100);
                status.setStatus("FAILED");
                status.setMessage("맞춤 학습 자료 생성에 실패하였습니다.\n다시 시도해 주세요.: " + e.getMessage());
            });

            log.error("맞춤 학습자료 생성 실패: {}", e.getMessage(), e);
            
            // @Async 비동기 처리로 최초 요청시에 클라이언트에서 예외상황을 알 수 없음
            throw new AppException("맞춤 학습자료 생성에 실패하였습니다.: " + e.getMessage());
        }
    }

}
