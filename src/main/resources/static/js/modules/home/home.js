import {renderStudyListCard} from "../study/study-renderer.js";
import {apiGetHomeUserDailyStudies} from "./home-api.js";

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
            const homeDailyStudies = await apiGetHomeUserDailyStudies(1, $studyListContainer);

            let content = "";
            homeDailyStudies.forEach((study, index) => {
                // 각 학습자료를 렌더링할 Html 생성
                content+= renderStudyListCard(study, index);
            });

            // 맞춤 학습 자료 추가
            $studyListContainer.empty(); // 초기화
            $studyListContainer.append(content); // 데이터 세팅
        } catch (error) {
            console.error("맞춤 학습 자료 요청 실패: ", error.message);
        }
    }

    // 맞춤 학습 자료 목록이 추가될 부모 요소가 있고
    // 로그인 + 학습 진도/단계 설정이 완료 되었을 경우 실행
    if ($studyListContainer.length && isStudyLevelSet) {
        await renderHomeDailyStudies(); // 실행
    }

    //////////// 아래 삭제 예정 /////////////////
    // await renderDailyStudy();
    //
    // async function renderDailyStudy(username) {
    //     const studyList = await apiGetDailyStudies(1);
    //
    //     studyList.forEach((study, index) => {
    //         // 각 학습자료를 렌더링할 Html 생성
    //         const studyCardHtml = renderStudyListCard(study, index);
    //         // 학습 목록 컨테이너에 html을 append
    //         $studyListContainer.append(studyCardHtml);
    //     });
    // }
})