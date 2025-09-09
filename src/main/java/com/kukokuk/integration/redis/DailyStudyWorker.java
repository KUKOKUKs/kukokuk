package com.kukokuk.integration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.common.dto.JobStatusResponse;
import com.kukokuk.common.store.RedisJobStatusStore;
import com.kukokuk.domain.study.dto.DailyStudyJobPayload;
import com.kukokuk.domain.study.dto.DailyStudySummaryResponse;
import com.kukokuk.domain.study.dto.UserStudyRecommendationDto;
import com.kukokuk.domain.study.service.StudyService;
import com.kukokuk.domain.study.vo.DailyStudy;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class DailyStudyWorker {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisJobStatusStore<DailyStudySummaryResponse> redisJobStatusStore;
    private final ObjectMapper objectMapper;
    private final StudyService studyService;

    /**
     * Redis 큐를 소비하는 worker
     * - BLPOP 방식으로 큐를 Blocking Polling
     * - Lettuce 커넥션 풀을 사용 (죽은 커넥션이면 새 커넥션 자동 대여)
     * - 무한 루프이지만, 예외 발생 시 catch로 잡고 재시도 (Worker 스레드가 죽지 않도록)
     * - BLPOP Timeout을 30초로 설정 (0=무한대기하면 커넥션 끊기면 못 회복하기 때문)
     */
    @Async("aiTaskExecutor") // 지정한 스레드풀에서 비동기 실행
    public void studyGenerateWorker() {
        // 서버가 켜져있는 동안, 계속 Redis 큐를 감시하도록 무한루프 설정
        /* 성능 문제?
            - Worker는 무한 루프이긴 하지만, BLPOP에서 막혀있기 때문에 실제로는 CPU 낭비 없음
        */
        while(true) {
            try {
                // Redis의 BLPOP (blocking pop)
                // 큐가 비어있으면 대기 (CPU 소모 X), 큐에 작업이 들어오면 즉시 응답
                // ++ redisTemplate의 역직렬화 설정하지 않았으므로 String으로 받아서 변환하기
                /*
                    leftPop(K key, long timeout, TimeUnit unit)
                    - key : Redis 리스트의 키 이름
                    - timeout : 리스트가 비어있을 경우, 최대 대기시간 설정 (0은 무한대기)
                    - unit : 대기시간의 단위(초, 밀리초 등)
                    -> 30초 : 30초 동안 새 job이 없으면 null qㅏㄴ환
                 */
                String jobJson = stringRedisTemplate.opsForList()
                    .leftPop("study:generate", 30, TimeUnit.SECONDS);

                // 큐에서 꺼낸 job처리
                if(jobJson != null) {
                    // 큐에서 꺼낸 JSON을 DailyStudyJobPayload로 변환
                    DailyStudyJobPayload payload =
                        objectMapper.readValue(jobJson, DailyStudyJobPayload.class);
                    log.info("Worker: Job 수신 = {}", payload);

                    // 실제 job을 처리하는 메소드 호출(AI 호출 + DB 저장 + 상태 갱신)
                    handleGenerateStudy(payload);
                }
            } catch (Exception e) {
                // Redis 서버가 꺼지거나 네트워크 오류 등으로 기존 Redis 연결이 종료되었을 때
                log.error("Worker error 발생. 5초 후 재시도", e);

                try {
                    Thread.sleep(5000); // 잠깐 쉬고 다시 시도 (CPU 100% 방지)
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 인터럽트 플래그 복구
                }
            }
        }
    }

    private void handleGenerateStudy(DailyStudyJobPayload payload) {
        try {
            // 멱등 체크 - 이미 학습자료가 DB에 존재하면 새로 만들지 않고 DONE 처리
            UserStudyRecommendationDto existDto = studyService.getDailyStudyByMaterial(payload.getDailyStudyMaterialNo(), payload.getStudyDifficultyNo());

            // 이미 학습자료가 DB에 존재하는 경우, 작업상태를 DONE으로 업데이트 및 데이터 추가
            if (existDto.getDailyStudy() != null) {
                redisJobStatusStore.update(payload.getJobId(), status -> {
                    status.setStatus("DONE");
                    status.setProgress(100);
                    status.setResult(studyService.mapToDailyStudySummaryResponse(existDto));
                    status.setMessage("이미 생성된 학습자료입니다");
                });
                return;
            }

            // AI 호출 작업 상태로 업데이트
            redisJobStatusStore.update(payload.getJobId(), status -> {
                status.setProgress(50);
                status.setMessage("맞춤 학습 자료 생성 중...");
            });

            // AI 호출 및 생성된 학습자료 DB 저장
            DailyStudy dailyStudy = studyService.createDailyStudy(payload.getDailyStudyMaterialNo(),
                payload.getStudyDifficultyNo());
            // 반환할 DTO에 새로 생성된 dailyStudy 주입
            existDto.setDailyStudy(dailyStudy);

            // AI 호출 및 DB 저장 완료 상태로 업데이트
            redisJobStatusStore.update(payload.getJobId(), status -> {
                status.setStatus("DONE");
                status.setProgress(100);
                status.setResult(studyService.mapToDailyStudySummaryResponse(existDto));
                status.setMessage("학습 자료 생성 완료");
            });
        } catch (Exception e) {
            log.error("학습자료 생성 중 에러 발생. payload={}, error={}", payload, e.getMessage(), e);

            // 작업상태 실패로 업데이트
            redisJobStatusStore.update(payload.getJobId(), status -> {
                status.setStatus("FAILED");
                status.setProgress(100);
                status.setMessage("맞춤 학습 자료 생성에 실패하였습니다.\n다시 시도해 주세요.: " + e.getMessage());
            });
        }
    }
}
