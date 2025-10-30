/**
 * 범용적으로 사용할 수 있는 백그라운드 상태 폴링 함수
 * <p>jobId: "material:12:difficulty:3",  // 작업 고유 ID</p>
 * <p>status: "PROCESSING" | "DONE" | "FAILED", // 현재 상태</p>
 * <p>progress: 50,                       // 진행률 (0~100)</p>
 * <p>message: "맞춤 학습 자료 생성 중...", // 상태 메시지</p>
 * <p>result: {...}                       // 최종 결과 (DONE일 때만 포함)</p>
 * @param {string} url - 상태를 폴링으로 조회할 API 엔드포인트
 * @param {jQuery} $statusElement - 폴링 진행 상태(진행률/메시지)를 추가/업데이트 할 요소
 * @param {boolean} isUseProcessUpdate - 폴링 진행 상태(진행률/메시지)를 추가/업데이트 사용 여부
 */
export function pollJobStatus(url, $statusElement = null, isUseProcessUpdate = true) {
    console.log("pollJobStatus() api 요청 실행");

    let reqCnt = 1;
    const interval = 1500; // 폴링 간격(ms)
    const timeout = 50000; // 최대 대기 시간(ms)

    return new Promise((resolve, reject) => {
        const startTime = Date.now();

        const poll = async () => {
            try {
                const statusResponse = await $.ajax({
                    method: "GET",
                    url: url,
                    dataType: "json"
                });

                // 폴링 요청 응답
                const pollJob = statusResponse.data;
                reqCnt++;
                console.log(`poll() [${pollJob.jobId}]: ${reqCnt}번째 api 요청 실행`);

                // 폴링 진행 상태(진행률/메시지) 추가/업데이트
                // isUseProcessUpdate === true 이고, $statusElement가 유효할 경우에만 호출
                if (isUseProcessUpdate && $statusElement && $statusElement.length) {
                    updatePollProcessing(pollJob, $statusElement);
                }

                // DONE/FAILED일 경우 처리완료로 반환
                // FAILED일 경우엔 클라이언트에서 조치
                if (pollJob.status !== "PROCESSING") {
                    console.log(`poll() [${pollJob.jobId}]: ${reqCnt}번째 api 요청 실행되었음 소요시간: ${Date.now() - startTime}ms`);
                    resolve(pollJob); // 최종적으로 여기서 Promise 해결
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

// 폴링 요청 함수에서만 사용될 상태 업데이트 함수
// 모든 폴링 요청 진행 상태(진행률/메시지) 추가/업데이트 (스피너 로딩)
function updatePollProcessing(pollJob, $statusElement) {
    console.log(`updatePollProcessing() 폴링 진행 상태 추가/업데이트 [${pollJob.jobId}]: `, pollJob.progress);
    const $loadingElement = $statusElement.find(".loading_spinner"); // 스피너 로딩 요소

    // 요소 확인
    if ($statusElement.length) {
        if ($loadingElement.length) {
            // 스피너 로딩 요소가 이미 생성되었다면 진행률, 메세지 수정
            $loadingElement.find(".info")
            .css("--percent", `${pollJob.progress}%`)
            .text(pollJob.message);
        } else {
            // 스피너 로딩 요소가 없다면 생성하여 적용
            $statusElement.html(
                `<div class="loading_spinner pd_base">
                        <p class="info" style="--percent: ${pollJob.progress}%;">${pollJob.message}</p>
                </div>`
            );
        }
    }
}