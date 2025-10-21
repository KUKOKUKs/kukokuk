package com.kukokuk.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 비동기 작업 전용 DTO로(비동기 백그라운드 작업 추적용)
 * <p>
 * 서버에서 백그라운드로 실행 중인 작업의
 * 진행 상태, 결과, 에러 메시지를 추적하고 관리하기 위한 구조
 * <p>
 * jobId로 폴링 가능
 * 처리 완료/실패 되면 반환 후 제거됨
 * @param <T>
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobStatusResponse<T> {

    private String jobId;       // 유니크 ID(uuid로 생성된 랜덤값 또는 지정된 유니크한 키설정)
    private String status;      // PROCESSING, DONE, FAILED(작업중, 완료, 실패)
    private Integer progress;   // 0~100(작업 진행률)
    private T result;           // 실제 작업 결과(스레드로 실행되어 반환할 타입 제네릭 설정)
    private String message;     // 에러 또는 안내 메시지

}
