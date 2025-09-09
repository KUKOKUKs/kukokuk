/**
 * 주어진 학습(study) 데이터를 기반으로 학습자료 목록에 표시할 카드 HTML을 생성하는 함수
 *      "dailyStudyNo": 1,
 *      "title": "문단 배우기: 중심 문장과 뒷받침 문장",
 *      "cardCount" : 3, // 일일학습의 총 카드 개수
 *      "status" : "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
 *      "studiedCardCount" : 2, // 해당 사용자가 이 일일학습에서 학습한 카드 개수
 *      "progressRate" : 66,
 *      "school" : "초등", // "초등", "중등",
 *      "grade" : 1,
 *      "sequence" : 3 // 학년 내 자료의 순서
 *    }
 * @param job
 * @param index 학습 카드의 인덱스 (목록 순서)
 * @param $studyListContainer
 * @returns {string} 완성된 학습 카드의 HTML 문자열
 */
export function renderStudyListCard(job, index, $studyListContainer) {

    const $existing = $studyListContainer.find(`[data-job-id="${job.jobId}"]`);

    let cardHtml = "";

    if (job.status === "PROCESSING") {
        cardHtml = `
            <div class="component base_list_component daily_study_card skeleton"
                data-job-id="${job.jobId}">
                <div class="study_info">
                    <div class="component_title skeleton-line"></div>
                    <div class="study_content skeleton-line"></div>
                    <div class="study_tag_list skeleton-line"></div>
                </div>
            </div>`;
    } else if (job.status === "FAILED") {
        cardHtml = `
            <div class="component base_list_component daily_study_card skeleton"
                data-job-id="${job.jobId}">
                <div class="study_info">
                    <div class="component_title skeleton-line"></div>
                    <div class="study_content skeleton-line"></div>
                    <div class="study_tag_list skeleton-line"></div>
                </div>
            </div>`;
    } else if (job.status === "DONE") {
        const study = job.result;

        // 학습 상태에 따라 표현할 텍스트 설정
        let statusText = '';
        const tagList = [];
        switch (study.status) {
            case 'NOT_STARTED':
                statusText = '오늘의 학습 시작하기';
                tagList.push({ label: "학습 전", color: "gray" });
                break;
            case 'IN_PROGRESS':
                statusText = '지난 학습 이어하기';
                tagList.push({ label: "학습 중", color: "red" });
                break;
            case 'COMPLETED':
                statusText = '복습하기';
                tagList.push({ label: "학습 완료", color: "green" });
                break;
        }

        // 서술형 퀴즈 완료 여부 체크
        if (study.essayQuizCompleted) {
            tagList.push({ label: "서술형 퀴즈 완료", color: "purple" });
        } else {
            tagList.push({ label: "서술형 퀴즈 미완료", color: "gray" });
        }

        // tagList를 HTML로 변환
        const tagHtml = tagList
        .map(tag => `<span class="study_tag ${tag.color}">${tag.label}</span>`)
        .join('');

        // 서술형 퀴즈 버튼을 강조하는 클래스를 설정하는 변수
        // 학습은 완료했지만 서술형퀴즈를 풀지 않은 경우에만 버튼 강조
        const essayBtnClass = (study.status === 'COMPLETED' && !study.essayQuizCompleted) ?
            'highlight' : '';

        // index가 0일때만 표시할 버튼 html구성
        const buttons = index === 0 ? `
                                    <div class="btn_list column">
                                      <a href="/study/${study.dailyStudyNo}" class="btn dark">${statusText}</a>
                                      <a href="/study/${study.dailyStudyNo}/essay" class="btn white ${essayBtnClass}">AI 피드백 기반 논술형 퀴즈 풀기</a>
                                    </div>
                                  ` : ``;

        // 최종적으로 조립된 학습 카드 HTML 반환
        cardHtml =`<div class="component base_list_component daily_study_card ${index === 0 ? 'no_cursor' : ''}"
                    data-study-no="${study.dailyStudyNo}"
                    data-study-status="${statusText}"
                    data-essay-btn-class="${essayBtnClass}"
                    data-job-id="${job.jobId}">
                    <div class="study_info">
                        <div class="component_title">${study.title}</div>
                        <div class="study_content">${study.explanation ? study.explanation : ''}</div>
                        <div class="study_tag_list">${tagHtml}</div>
                    </div>
                    <div class="level_info study_list">
                        <div class="bar_gauge">
                            <div class="gauge" style="width: ${study.progressRate}%"></div>
                        </div>
                    </div>
                    ${buttons}
                </div>
              `;
    }

    if ($existing.length) {
        $existing.replaceWith(cardHtml);
    } else {
        $studyListContainer.append(cardHtml);
    }
}

export function renderStudyListCardSkeleton(jobId, $studyListContainer) {
    const skeletionHtml =  `<div class="component base_list_component daily_study_card ${index === 0 ? 'no_cursor' : ''}"
                data-job-id="${jobId}">
                <div class="study_info">
                    <div class="component_title">${study.title}</div>
                    <div class="study_content">${study.explanation ? study.explanation : ''}</div>
                    <div class="study_tag_list">${tagHtml}</div>
                </div>
                <div class="level_info study_list">
                    <div class="bar_gauge">
                        <div class="gauge" style="width: ${study.progressRate}%"></div>
                    </div>
                </div>
                ${buttons}
            </div>
          `;

    $studyListContainer.append(skeletionHtml);
}
