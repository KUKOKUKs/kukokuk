// noinspection ES6UnusedImports

import {apiErrorProcessByXhr} from '../../utils/api-error-util.js';
import {updateHomeDailyStudyProgressUI} from "./home-handler.js";

/**
 * 요청할 자료 수를 전달받아 맞춤 학습 자료 비동기 요청
 * <p>
 *     JobStatusResponse<List<DailyStudySummaryResponse>> 객체 응답으로
 *     폴링 처리 하여 진행 상태 확인
 * @param dailyStudyCount 요청할 자료 수
 * @param $studyListContainer 진행률이 표시될 부모 요소
 * @returns 맞춤 학습 자료 목록
 */
export async function apiGetHomeUserDailyStudies(dailyStudyCount, $studyListContainer) {
    console.log("apiGetHomeUserDailyStudies() api 요청 실행");
    try {
        // 최초 요청
        const response = await $.ajax({
            method: "GET",
            url: "/api/home/studies",
            data: {dailyStudyCount},
            dataType: "json",
        });

        console.log("apiGetHomeUserDailyStudies() api 요청 response: ", response);
        
        // 최초 응답
        const jobStatus = response.data; // JobStatusResponse
        const jobId = jobStatus.jobId;

        // 진행률 및 메시지 UI 업데이트
        updateHomeDailyStudyProgressUI(jobStatus.progress, jobStatus.message, $studyListContainer);

        // DONE이면 바로 반환
        if (jobStatus.status === "DONE") {
            console.log("이미 완료된 상태로 DB데이터 반환:", jobStatus.result);
            return jobStatus.result;
        }

        // PROCESSING이면 폴링 시작(await으로 종료될 때까지 상위 호출부로 반환되지 않음)
        return await pollJobStatus(jobId, $studyListContainer);
    } catch (error) {
        if (error.responseJSON) {
            // 서버에서 예외처리된 에러
            apiErrorProcessByXhr(error.responseJSON);
        } else {
            // 폴링에서 타임아웃 또는 알 수 없는 오류
            console.error(error);
            throw erorr;
        }
    }
}

/**
 * 백그라운드 상태 폴링
 * @param {string} jobId Redis Job Key
 * @param $studyListContainer 진행률이 표시될 부모 요소
 */
export function pollJobStatus(jobId, $studyListContainer) {
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
                    url: `/api/home/studies/status/${jobId}`,
                    dataType: "json"
                });

                // 폴링 요청 응답
                const status = statusResponse.data;

                // 진행률 및 메시지 UI 업데이트
                updateHomeDailyStudyProgressUI(status.progress, status.message, $studyListContainer);

                // DONE/FAILED일 경우 처리완료로 반환
                // FAILED일 경우엔 클라이언트에서 조치
                if (status.status === "DONE" || status.status === "FAILED") {
                    console.log(`poll() ${reqCnt}번째 api 요청 실행되었음 소요시간: ${Date.now() - startTime}ms`);
                    resolve(status.result); // 최종적으로 여기서 Promise 해결
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