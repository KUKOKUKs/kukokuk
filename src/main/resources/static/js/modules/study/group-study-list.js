import {apiGetGroupStudies} from "./study-api.js";
import {
    renderStudyCardContent,
    renderStudyListSkeleton
} from "./study-renderer.js";

$(document).ready(async () => {
    // 그룹 학습 자료 목록 프레그먼츠로 사용되는 학습 자료 관련
    const $groupStudyListContainer = $('#group-study-list-container'); // 학습 자료가 생성될 부모 요소
    const groupStudyRows = Number($groupStudyListContainer.data("gruop-study-rows") || 1); // 사용할 학습 자료 개수
    const isUseSpinner = Boolean($groupStudyListContainer.data("is-spinner") || false); // 진행 상태 스피너 로딩 사용여부(기본 스켈레톤 로딩)

    // 그룹 학습 자료 목록 요청하여 리스트 추가
    async function renderGroupStudies() {
        console.log("renderGroupStudies() 실행");

        // 로딩 표시(html 교체로 요소가 업데이트 되어서 로딩 제거가 필요 없음)
        renderStudyListSkeleton(groupStudyRows, isUseSpinner);

        try {
            const grouopStudies = await apiGetGroupStudies(groupStudyRows);

            // 학습 카드 생성
            let content = "";
            if (grouopStudies.length > 0) {
                // 학습 자료가 등록되어 있는 경우 목록 세팅
                grouopStudies.forEach((study, index) => {
                    content += `
                        <div class="component base_list_component study_card ${index !== 0 ? 'close' : ''}">
                            ${renderStudyCardContent(study)}
                        </div>
                    `;
                });
            } else {
                // 학습 자료가 등록되지 않은 경우
                content += `
                    <div class="component base_list_component study_card">
                        <div class="study_info">
                            <div class="component_title">우리반 학습 자료</div>
                            <div class="study_content">아직 등록된 우리반 학습 자료가 없습니다.</div>
                        </div>
                    </div>
                `;
            }

            $groupStudyListContainer.html(content); // 해당 요소에 컨텐츠 추가
        } catch (error) {
            console.error("그룹 학습 자료 요청 실패: ", error.message);
            $groupStudyListContainer.html(
                `<div class="component base_list_component study_card">
                    <div class="study_info">
                        <div class="component_title">오류가 발생하였습니다</div>
                        <div class="study_content">학습 자료 목록을 가져오는데 실패하였습니다. 다시 시도해 주세요.</div>
                    </div>
                </div>`
            );
        }
    }
    
    // 학습 자료 목록이 추가될 부모 요소가 있을 경우 실행
    if ($groupStudyListContainer.length) {
        await renderGroupStudies(); // 실행
    }
})