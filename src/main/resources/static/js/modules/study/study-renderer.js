/**
 * 주어진 학습(study) 데이터를 기반으로 학습자료 목록에 표시할 카드 HTML을 생성하는 함수
 * <p>"dailyStudyNo": 1</p>
 * <p>"title": "문단 배우기: 중심 문장과 뒷받침 문장",</p>
 * <p>"cardCount" : 3, // 일일학습의 총 카드 개수</p>
 * <p>"status" : "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"</p>
 * <p>"studiedCardCount" : 2, // 해당 사용자가 이 일일학습에서 학습한 카드 개수</p>
 * <p>"progressRate" : 66,</p>
 * <p>"school" : "초등", // "초등", "중등",</p>
 * <p>"grade" : 1,</p>
 * <p>"sequence" : 3 // 학년 내 자료의 순서</p>
 * @param job 학습 자료 생성 정보가 담겨 있는 객체
 * @param index 학습 카드의 인덱스 (첫 번째 요소인지 확인하기 위해)
 * @param $studyCard 카드 랜더링할 요소
 */
export function renderStudyCard(job, index, $studyCard) {
    console.log("renderStudyCard() 실행 순번: ", index);
    if (!$studyCard.length) return;

    if (job.status === "FAILED") {
        // FAILED일때의 ERROR 카드 UI
        const failedHtml = `
            <div class="study_info">
                <div class="component_title">자료 생성 중 오류가 발생하였습니다</div>
                <div class="study_content">처리 중인 작업이 완료된 후 다시 시도해 주세요</div>
            </div>
        `;
        $studyCard.html(failedHtml);
    } else if (job.status === "DONE") {
        const study = job.result;
        $studyCard.html(renderStudyCardContent(study)); // 해당 요소에 컨텐츠 추가
        if (index !== 0) $studyCard.addClass("close"); // 첫 번째 요소가 아니라면 커서 닫힌 상태
    }
}

// 학습 자료 정보로 학습 카드 내용 생성하여 반환(텍스트 기반)
export function renderStudyCardContent(study) {
    console.log("renderStudyCardContent() 실행");

    // 학습 상태 태그 설정 NOT_STARTED, IN_PROGRESS, COMPLETED
    const studyStatus = `학습 ${study.status === 'NOT_STARTED' ? '전' : study.status === 'IN_PROGRESS' ? '중' : '완료'}`;
    let tagHtml = `<span class="study_tag ${study.status.toLowerCase()}">${studyStatus}</span>`;

    // 서술형 상태 태그 설정
    const essayStatus = `서술형 ${study.essayQuizCompleted ? '완료' : '미완료'}`;
    tagHtml += `<span class="study_tag ${study.essayQuizCompleted ? '' : 'essay_undone'}">${essayStatus}</span>`;

    // 학습 완료 후 서술형 완료 여부
    const isNotEssay = study.status === 'COMPLETED' && !study.essayQuizCompleted;

    // 버튼 색상,텍스트 설정
    const studyBtnText = study.status === 'NOT_STARTED'
                                ? '오늘의 학습 시작하기'
                                : study.status === 'IN_PROGRESS'
                                    ? '지난 학습 이어하기'
                                    : '복습하기';
    const studyBtnColor = study.status === 'COMPLETED'
                                    ? ''
                                    : 'primary';

    return `
        <div class="study_info">
            <div class="component_title">${study.title}</div>
            <div class="study_content">${study.explanation || ''}</div>
            <div class="study_tag_list">
                ${tagHtml}
            </div>
            <div class="bar_gauge">
                <div class="gauge" style="width: ${study.progressRate}%;"></div>
            </div>
            <div class="btn_list column study_card_btns">
                <a href="/study/${study.dailyStudyNo}" class="btn ${studyBtnColor}">${studyBtnText}</a>
                <a href="/study/${study.dailyStudyNo}/essay" class="btn ${isNotEssay ? 'full_blue highlight' : 'blue'}">
                    AI 피드백 기반 논술형 문제 풀기
                </a>
            </div>
        </div>
    `;
}

// 스켈레톤 로딩 표시 세팅(유연하게 처리하기 위해 내용만 생성하여 반환/직접 세팅하지 않음)
export function renderStudyListSkeleton(jobStatusList, isUseSpinner = true) {
    console.log("renderStudyListSkeleton() 실행");

    // 숫자가 들어온 경우 해당 개수만큼 빈 배열 생성
    if (typeof jobStatusList === 'number') {
        jobStatusList = Array.from({ length: jobStatusList }, (_, i) => ({
            jobId: i, // 각 skeleton에 index data-job-id 부여
        }));
    }

    let content = '';
    for (const job of jobStatusList) {
        content += isUseSpinner
            ? `<div class="component base_list_component study_card" 
                    data-job-id="${job.jobId}">
                <div class="study_info">
                    <div class="loading_spinner">
                        <p class="info" style="--percent: 0%;">학습 자료 가져오는 중...</p>
                    </div>
                </div>
            </div>`
            : `<div class="component base_list_component study_card"
                    data-job-id="${job.jobId}">
                <div class="study_info">
                    <div class="component_title skeleton _w70"></div>
                    <div class="study_content d_flex column tiny_gap">
                        <p class="skeleton"></p>
                        <p class="skeleton _w70"></p>
                    </div>
                    <div class="study_tag_list">
                        <div class="study_tag skeleton _w30"></div>
                        <div class="study_tag skeleton _w30"></div>
                    </div>
                    <div class="bar_gauge skeleton"></div>
                    <div class="btn skeleton"></div>
                </div>
            </div>`
    }

    return content;
}