
/*
    정호형님 확인 후 삭제 !!!
    home-handler대신 study-poll.js로 옮김
    그리고 이 렌더링 UI 수정 필요 ( 기존에는 목록 전체에서 스피너 하나 돌아가는 방식이었으나,
    이젠 카드별로 로딩 혹은 스켈레톤 UI로 수정해야함
 */
/**
 * 백그라운드 작업 처리되는
 * JobStatusResponse<List<DailyStudySummaryResponse>>를 활용한 
 * 비동기 요청에 대해 진행률과 메세지 표시(스피너 로딩 요소 사용) 설정
 * @param progress 진행률
 * @param message 메세지
 * @param $studyListContainer 진행률이 표시될 부모 요소
 */
/*
export function updateHomeDailyStudyProgressUI(progress, message, $studyListContainer) {
    const $loadingElement = $studyListContainer.find(".loading_spinner"); // 스피너 로딩 요소

    // 부모 요소 확인
    if ($studyListContainer.length) {
        if ($loadingElement.length) {
            // 스피너 로딩 요소가 이미 생성되었다면 진행률, 메세지 수정
            $loadingElement.find(".info")
                .css("--percent", `${progress}%`)
                .text(message);
        } else {
            // 스피너 로딩 요소가 없다면 생성하여 적용
            $studyListContainer.html(
                `<div class="component base_list_component">
                    <div class="loading_spinner pd_base">
                        <p class="info" style="--percent: ${progress}%;">${message}</p>
                    </div>
                </div>`
            );
        }
    }
}
*/