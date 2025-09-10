import {apiGetDailyStudies} from "./study-api.js";

// 학습 자료 카드가 삽입될 목록 컨테이너
const $studyListContainer = $('.study_list_container');
$(document).ready(async () => {
    // userInfo(사용자의 학습수준/진도)가 모두 null이 아니면 ready는 true
    const ready = userInfo.difficulty && userInfo.school && userInfo.grade;
    if (ready) {
        const rows = 10;
        // 학습 수준이 준비된 경우 학습자료 목록 조회
        await getDailyStudies(rows);
    } else {
        // 학습 수준이 설정되지 않은 경우 학습수준설정 버튼 렌더링
        renderSetUpBtn();
    }
});

/**
 * 화면이 렌더링되는 시점에 사용자의 학습자료 목록 조회 및 조회할 학습자료 부족시 추가생성하는 API 요청
 */
async function getDailyStudies(rows) {
    try {
        // 응답을 받기 전까지 로딩 컴포넌트를 표시
        $studyListContainer.html(`
                        <div class="loading_spinner full_height">
                            <div class="spinner"></div>
                            <div class="info_text">사용자 맞춤 학습자료 불러오는 중...</div>
                        </div>
                    `);

        // 사용자 맞춤 학습자료 목록을 조회하는 rest API 요청 호출
        const jobStatusList = await apiGetDailyStudies(rows, $studyListContainer);

        // 조회된 학습자료가 없는 경우
        if (jobStatusList.length === 0) {
            $studyListContainer.empty().html(`
                <div class="loading_spinner">
                    <div class="info_text">더이상 조회할 학습자료가 없습니다. 학습수준과 진도를 변경해서 학습해보세요. </div>
                </div>
                `);
        }
    } catch (err) {
        // 에러 발생 시 에러메세지 렌더링
        $studyListContainer.html(`
                            <div class="loading_spinner">
                                <div class="info_text">학습자료 생성 및 조회에 실패했습니다. 다시 시도해주세요. </div>
                            </div>
                            `);
    }
}

/**
 * 사용자의 학습수준/진도가 설정되어 있지 않을 때 버튼 렌더링
 *
 */
function renderSetUpBtn($studyListContainer) {
    $studyListContainer.append(`
        <button class="modal_study_level_btn btn white">
        학습수준 설정하고 일일학습하기
        </button>
    `);
}

/**
 * 학습 자료 카드를 클릭했을 때, 안에 study_btn_list가 없다면 동적으로 추가하는 이벤트 핸들러
 */
$(document).on('click', '.daily_study_card', function () {

    // 이미 버튼이 있으면 추가하지 않음
    if ($(this).find('.btn_list').length === 0) {
        // 커스텀 속성에서 상태 텍스트 가져오기
        const statusText = $(this).attr('data-study-status');
        const dailyStudyNo = $(this).attr('data-study-no');
        const essayBtnClass = $(this).attr('data-essay-btn-class');

        // 버튼 HTML
        const buttonsHtml = `
            <div class="btn_list column">
              <a href="/study/${dailyStudyNo}" class="btn dark">${statusText}</a>
              <a href="/study/${dailyStudyNo}/essay" class="btn white ${essayBtnClass}">AI 피드백 기반 논술형 퀴즈 풀기</a>
            </div>
          `;

        // 마지막에 append
        $(this).append(buttonsHtml);

        // 현재 카드의 포인터 커서가 동작하지 않도록 함
        $(this).addClass('no_cursor');
    }
})