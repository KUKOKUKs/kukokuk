import {renderFragmentDailyStudies} from "./study-poll.js";

$(document).ready(async () => {
    // 일반 학습 목록 프레그먼츠로 사용되는 학습 자료 관련
    const $studyListContainer = $('#study-list-container'); // 학습 자료가 생성될 부모 요소
    const studyRows = Number($studyListContainer.data("study-rows") || 1); // 사용할 학습 자료 개수
    const requestRows = studyRows > 5 ? studyRows : 5; // 기본 요청 데이터 개수는 컨트롤러 기본 값 5
    const isUseSpinner = Boolean($studyListContainer.data("is-spinner") || false); // 진행 상태 스피너 로딩 사용여부(기본 스켈레톤 로딩)

    // 학습 자료 목록이 추가될 부모 요소가 있을 경우 실행
    if ($studyListContainer.length) {
        await renderFragmentDailyStudies(studyRows, requestRows, $studyListContainer, isUseSpinner); // 실행
    }
})