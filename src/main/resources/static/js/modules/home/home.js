
import {apiGetDailyStudies} from "../study/study-api.js";

$(document).ready(async () => {
    // 홈 화면 일일 맞춤 학습 자료 관련
    const $studyListContainer = $('.study_list_container'); // 맞춤 학습 자료가 생성될 부모 요소
    // 맞춤 학습 자료 요청 가능 상태 여부(인증 완료 + 학습 진도/단계 설정 완료)
    const isStudyLevelSet = $studyListContainer.data("is-study-level-set");

    // 맞춤 학습 자료 요청하여 홈 화면에 추가
    async function renderHomeDailyStudies() {
        console.log("renderHomeDailyStudies() 실행");

        try {
            // 홈 화면에 제공할 일일 맞춤 학습 자료 1개 요청
            // 최초 요청 후 폴링하며 진행 상태 표시
            await apiGetDailyStudies(1, $studyListContainer);

        } catch (error) {
            console.error("맞춤 학습 자료 요청 실패: ", error.message);
        }
    }

    // 맞춤 학습 자료 목록이 추가될 부모 요소가 있고
    // 로그인 + 학습 진도/단계 설정이 완료 되었을 경우 실행
    if ($studyListContainer.length && isStudyLevelSet) {
        await renderHomeDailyStudies(); // 실행
    }
})