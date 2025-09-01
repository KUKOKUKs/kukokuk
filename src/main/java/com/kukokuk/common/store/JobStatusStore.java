package com.kukokuk.common.store;

import com.kukokuk.common.dto.JobStatusResponse;
import java.util.function.Consumer;
 
/**
 * 범용 Job 상태 저장소 인터페이스(작업 진행중 상태 관리 용도로 처리완료/실패 후 제거 가능)
 * @param <T> JobStatusResponse의 result 리스트 타입
 */
public interface JobStatusStore<T> {

    /**
     * 상태 저장
     * @param status 저장할 JobStatusResponse<T> 객체
     *               - jobId: 유니크 ID (Redis Key)
     *               - status: PROCESSING, DONE, FAILED
     *               - progress: 0~100 진행률
     *               - result: 실제 작업 결과
     *               - message: 진행 상태 메시지 또는 에러 메시지
     */
    void put(JobStatusResponse<T> status);

    /**
     * 상태 조회
     * @param jobId 조회할 Job ID
     * @return JobStatusResponse<T> 객체
     *         - Redis에 값이 없으면 null 반환
     *         - Object로 저장되어 있으므로 캐스팅 필요
     */
    JobStatusResponse<T> get(String jobId);

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
    void update(String jobId, Consumer<JobStatusResponse<T>> updater);

    /**
     * 상태 삭제
     * @param jobId 제거할 Job ID
     * <p>
     * - 작업 완료 후, 실패 시 재요청 가능하도록 Redis에서 Key 삭제
     * - 중복 요청 방지
     */
    void delete(String jobId);

}
