import {pollJobStatus} from "../../utils/poll-job-utils.js";

export function pollStudyJob(jobId, $studyCardContainer) {
    return pollJobStatus(
        `/api/studies/status/${jobId}`,
        jobStatus => {
            // 진행률 및 메시지 UI 업데이트
            updateHomeDailyStudyProgressUI(jobStatus.progress, jobStatus.message, $studyCardContainer);
        }
    )
}

/**
 * 백그라운드 작업 처리되는
 * JobStatusResponse<List<DailyStudySummaryResponse>>를 활용한
 * 비동기 요청에 대해 진행률과 메세지 표시(스피너 로딩 요소 사용) 설정
 * @param progress 진행률
 * @param message 메세지
 * @param $studyCardContainer 진행률이 표시될 부모 요소
 */
export function updateHomeDailyStudyProgressUI(progress, message, $studyCardContainer) {
    const $loadingElement = $studyCardContainer.find(".loading_spinner"); // 스피너 로딩 요소

    // 부모 요소 확인
    if ($studyCardContainer.length) {
        if ($loadingElement.length) {
            // 스피너 로딩 요소가 이미 생성되었다면 진행률, 메세지 수정
            $loadingElement.find(".info")
            .css("--percent", `${progress}%`)
            .text(message);
        } else {
            // 스피너 로딩 요소가 없다면 생성하여 적용
            $studyCardContainer.html(
                `<div class="loading_spinner pd_base">
                        <p class="info" style="--percent: ${progress}%;">${message}</p>
                </div>`
            );
        }
    }
}