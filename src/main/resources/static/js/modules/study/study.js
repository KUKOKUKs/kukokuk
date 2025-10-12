import {apiGetDailyStudies} from "./study-api.js";

$(document).ready(async () => {
    // 학습 자료 컨텐츠 관련
    const $studyListContainer = $('.study_list_container'); // 맞춤 학습 자료가 생성될 부모 요소
    // 맞춤 학습 자료 요청 가능 상태 여부(인증 완료 + 학습 진도/단계 설정 완료)
    const isStudyLevelSet = $studyListContainer.data("is-study-level-set");

    // 맞춤 학습 자료 요청하여 일일 학습 자료 목록 화면에 추가
    async function renderDailyStudies() {
        console.log("renderDailyStudies() 실행");

        try {
            const rows = 10;
            await apiGetDailyStudies(rows, $studyListContainer);
        } catch (error) {
            // 요청에 대한 에러처리(폴링에 대한 FAILED 또는 에러처리 아님)
            $studyListContainer.html(`
                <div class="component base_list_component daily_study_card">
                    <div class="study_info">
                        <div class="component_title">자료 생성 중 오류가 발생하였습니다</div>
                        <div class="study_content">잠시 후에 다시 시도해 주세요</div>
                    </div>
                </div>
            `);
        }
    }

    // 맞춤 학습 자료 목록이 추가될 부모 요소가 있고
    // 로그인 + 학습 진도/단계 설정이 완료 되었을 경우 실행
    if ($studyListContainer.length && isStudyLevelSet) {
        // 학습 수준이 준비된 경우 학습자료 목록 조회
        await renderDailyStudies();
    }

    // 학습 카드 클릭 이벤트 핸들러
    $(document).on('click', '.daily_study_card', function () {
        // 클릭한 요소가 close 상태일 경우 클래스 제거
        // 클래스 제거 효과: 컨텐츠 줄임표시 원상 복구, 버튼 노출
        const $this = $(this);
        const isClosed = $this.hasClass('close');
        if (isClosed) $this.removeClass('close');
    });
});

// 비동기 요청, 폴링, 랜더 처리 함수 내에서 자동호 되어 있으므로 아래 함수는 필요 없음
// /**
//  * 화면이 렌더링되는 시점에 사용자의 학습자료 목록 조회 및 조회할 학습자료 부족시 추가생성하는 API 요청
//  */
// async function getDailyStudies(rows) {
//     try {
//         // 응답을 받기 전까지 로딩 컴포넌트를 표시
//         $studyListContainer.html(`
//                     <div class="loading_spinner full_height">
//                         <div class="info_text">사용자 맞춤 학습자료 생성 중...</div>
//                     </div>
//                 `);
//
//         // 사용자 맞춤 학습자료 목록을 조회하는 rest API 요청 호출
//         const jobStatusList = await apiGetDailyStudies(rows, $studyListContainer);
//
//         // 조회된 학습자료가 없는 경우
//         if (jobStatusList.length === 0) {
//             $studyListContainer.empty().html(`
//                 <div class="loading_spinner">
//                     <div class="info_text">더이상 조회할 학습자료가 없습니다. 학습수준과 진도를 변경해서 학습해보세요. </div>
//                 </div>
//                 `);
//         }
//     } catch (err) {
//         // 에러 발생 시 에러메세지 렌더링
//         $studyListContainer.html(`
//                             <div class="loading_spinner">
//                                 <div class="info_text">학습자료 생성 및 조회에 실패했습니다. 다시 시도해주세요. </div>
//                             </div>
//                             `);
//     }
// }

// 타임리프로 설정 가능하므로 아래 함수 필요 없음
// /**
//  * 사용자의 학습수준/진도가 설정되어 있지 않을 때 버튼 렌더링
//  *
//  */
// function renderSetUpBtn($studyListContainer) {
//     $studyListContainer.append(`
//         <button class="modal_study_level_btn btn white">
//         학습수준 설정하고 일일학습하기
//         </button>
//     `);
// }

// 이미 처리된 내용을 핸들러로 클래스 조작만 하도록 간편화
// $(this)를 함수내에서 반복적 사용할 경우 변수에 담아 활용
// 그렇지 않으면 $(this)를 사용할 때마다 제이쿼리 this 객체 생성되며 큐에 누적 됨
// 굉장히 안좋은 방법으로 메모리 낭비 및 브라우저 속도 저하 발생
// 제이쿼리에서 $(this) 사용할 일이 있을 경우 const $this = $(this); 습관화
// /**
//  * 학습 자료 카드를 클릭했을 때, 안에 study_btn_list가 없다면 동적으로 추가하는 이벤트 핸들러
//  */
// $(document).on('click', '.daily_study_card', function () {
//
//     // 이미 버튼이 있으면 추가하지 않음
//     if ($(this).find('.btn_list').length === 0) {
//         // 커스텀 속성에서 상태 텍스트 가져오기
//         const statusText = $(this).attr('data-study-status');
//         const dailyStudyNo = $(this).attr('data-study-no');
//         const essayBtnClass = $(this).attr('data-essay-btn-class');
//
//         // 버튼 HTML
//         const buttonsHtml = `
//             <div class="btn_list column">
//               <a href="/study/${dailyStudyNo}" class="btn dark">${statusText}</a>
//               <a href="/study/${dailyStudyNo}/essay" class="btn white ${essayBtnClass}">AI 피드백 기반 논술형 퀴즈 풀기</a>
//             </div>
//           `;
//
//         // 마지막에 append
//         $(this).append(buttonsHtml);
//
//         // 현재 카드의 포인터 커서가 동작하지 않도록 함
//         $(this).addClass('no_cursor');
//     }
// })