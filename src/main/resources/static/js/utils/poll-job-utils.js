
/**
 * 범용적으로 사용할 수 있는 백그라운드 상태 폴링 함수
 * @param {string} url - 상태를 폴링으로 조회할 API 엔드포인트
 * @param {Function} onUpdate - 진행률/메시지 갱신 함수 (jobStatus 객체를 인자로 받음)
 *      - 호출 시 전달되는 status 객체 예시:
 *        {
 *          jobId: "material:12:difficulty:3",  // 작업 고유 ID
 *          status: "PROCESSING" | "DONE" | "FAILED", // 현재 상태
 *          progress: 50,                       // 진행률 (0~100)
 *          message: "맞춤 학습 자료 생성 중...", // 상태 메시지
 *          result: {...}                       // 최종 결과 (DONE일 때만 포함)
 *        }
 */
export function pollJobStatus(url, onUpdate) {
    console.log("pollJobStatus() api 요청 실행");

    let reqCnt = 1;
    const interval = 1000; // 폴링 간격(ms)
    const timeout = 30000; // 최대 대기 시간(ms)

    return new Promise((resolve, reject) => {
        const startTime = Date.now();

        const poll = async () => {
            try {
                reqCnt++;
                console.log(`poll() ${reqCnt}번째 api 요청 실행`);

                const statusResponse = await $.ajax({
                    method: "GET",
                    url: url,
                    dataType: "json"
                });

                // 폴링 요청 응답
                const status = statusResponse.data;

                // UI 업데이트 콜백 실행
                if (onUpdate) {
                    onUpdate(status);
                }

                // DONE/FAILED일 경우 처리완료로 반환
                // FAILED일 경우엔 클라이언트에서 조치
                if (status.status === "DONE" || status.status === "FAILED") {
                    console.log(`poll() ${reqCnt}번째 api 요청 실행되었음 소요시간: ${Date.now() - startTime}ms`);
                    resolve(status); // 최종적으로 여기서 Promise 해결
                    return;
                }

                // 타임아웃 체크
                if (Date.now() - startTime > timeout) {
                    reject(new Error("요청이 오래 걸려 종료되었습니다."));
                    return;
                }

                // 다음 폴링(void로 Promise 무시 의도 명시/IDE 경고 제거)
                setTimeout(() => void poll(), interval);
            } catch (xhr) {
                reject(xhr);
            }
        };

        void poll(); // 최초 실행(void로 Promise 무시 의도 명시/IDE 경고 제거)
    });
}