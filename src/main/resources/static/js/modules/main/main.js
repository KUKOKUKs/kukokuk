import {apiGetDailyStudies} from "../study/study-api.js";
import {renderStudyListCard} from "../study/study-renderer.js";

$(document).ready(async () => {
    const $studyListContainer = $('.study_list_container');

    await renderDailyStudy();

    async function renderDailyStudy(username) {
        const studyList = await apiGetDailyStudies(1);

        studyList.forEach((study, index) => {
            // 각 학습자료를 렌더링할 Html 생성
            const studyCardHtml = renderStudyListCard(study, index);
            // 학습 목록 컨테이너에 html을 append
            $studyListContainer.append(studyCardHtml);
        });
    }
})