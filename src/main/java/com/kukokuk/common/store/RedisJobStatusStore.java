package com.kukokuk.common.store;

import com.kukokuk.common.dto.JobStatusResponse;
import java.time.Duration;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 기반 범용 Job 상태 저장소 구현체
 * (작업 진행중 상태 관리 용도로 처리완료/실패 후 제거 가능)
 * <p>
 * - Redis를 활용하여 진행 중인 Job 상태를 저장하고 조회, 업데이트
 * - TTL(Time To Live)을 설정하여 일정 시간 후 자동 만료
 * - JobStatusResponse<T>의 result 타입에 따라 유연하게 사용 가능
 * @param <T> JobStatusResponse의 result 리스트 타입
 */
@RequiredArgsConstructor // 초기화 되지않은 final 필드나, @NonNull 이 붙은 필드에 대해 생성자를 생성
@Component("redisJobStatusStore") // Bean 이름 명시, Service에서 @Qualifier로 주입 가능
public class RedisJobStatusStore<T> implements JobStatusStore<T> {

    /**
     * RedisTemplate 객체
     * - Key: String (jobId)
     * - Value: Object (JobStatusResponse<T> 객체를 Object로 저장)
     * - Object로 지정하여 다양한 타입의 JobStatusResponse<T>를 하나의 RedisTemplate에서 재사용 가능
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * TTL(Time To Live) 설정
     * - Redis에 저장된 상태는 15분 후 자동 만료
     * - TTL 적용으로 오래된 상태가 Redis에 남지 않고 자동 삭제됨
     */
    private final Duration ttl = Duration.ofMinutes(15);

    /**
     * 상태 저장
     * @param status 저장할 JobStatusResponse<T> 객체
     *               - jobId: 유니크 ID (Redis Key)
     *               - status: PROCESSING, DONE, FAILED
     *               - progress: 0~100 진행률
     *               - result: 실제 작업 결과
     *               - message: 진행 상태 메시지 또는 에러 메시지
     * Redis에 TTL 적용
     */
    @Override
    public void put(JobStatusResponse<T> status) {
        // Redis opsForValue().set(key, value, timeout)
        // - 단일 값 저장 + TTL 적용
        redisTemplate.opsForValue().set(status.getJobId(), status, ttl);
    }

    /**
     * 상태 조회
     * @param jobId 조회할 Job ID
     * @return JobStatusResponse<T> 객체
     *         - Redis에 값이 없으면 null 반환
     *         - Object로 저장되어 있으므로 캐스팅 필요
     */
    @Override
    @SuppressWarnings("unchecked")
    public JobStatusResponse<T> get(String jobId) {
        Object value = redisTemplate.opsForValue().get(jobId); // Redis에서 Object 가져오기
        if (value == null) return null;                        // 값 없으면 null 반환
        return (JobStatusResponse<T>) value;                   // Object -> JobStatusResponse<T>로 캐스팅
    }

    /**
     * 상태 업데이트
     * @param jobId 업데이트할 Job ID
     * @param updater Consumer<JobStatusResponse<T>> 객체를 받아 수정하는 람다
     * <p>
     * 사용 예시:
     *  update(jobId, status -> {
     *      status.setProgress(50);
     *      status.setMessage("중간 처리 완료");
     *  });
     * </p>
     * - 기존 객체 조회 후 updater 실행
     * - 수정 후 다시 Redis에 저장 + TTL 유지
     * - 존재하지 않으면 아무 동작 없음
     */
    @Override
    @SuppressWarnings("unchecked")
    public void update(String jobId, Consumer<JobStatusResponse<T>> updater) {
        Object value = redisTemplate.opsForValue().get(jobId); // 현재 상태 조회
        if (value != null) {                                   // 값이 존재하면
            JobStatusResponse<T> status = (JobStatusResponse<T>) value; // 캐스팅
            updater.accept(status);                             // 람다로 수정
            redisTemplate.opsForValue().set(jobId, status, ttl); // 수정 후 다시 저장 + TTL 유지
        }
    }

    /**
     * 상태 삭제
     * @param jobId 제거할 Job ID
     * <p>
     * - 작업 완료 후, 실패 시 재요청 가능하도록 Redis에서 Key 삭제
     * - 중복 요청 방지
     */
    @Override
    public void delete(String jobId) {
        redisTemplate.delete(jobId); // Redis Key 삭제
    }

}
