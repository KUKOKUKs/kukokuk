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
 * // @returns {string} 완성된 학습 카드의 HTML 문자열
 * @returns html 세팅이지 반환하는 값은 없음
 */
export function renderStudyListCard(job, index, $studyListContainer) {
    // 해당하는 학습 카드 찾기
    const $existingCard = $studyListContainer.find(`[data-job-id="${job.jobId}"]`);
    
    if (job.status === "FAILED") {
        // FAILED일때의 ERROR 카드 UI
        const failedHtml = `
            <div class="study_info">
                <div class="component_title">자료 생성 중 오류가 발생하였습니다</div>
                <div class="study_content">처리 중인 작업이 완료된 후 다시 시도해 주세요</div>
            </div>
        `;
        $existingCard.html(failedHtml);
    } else if (job.status === "DONE") {
        const study = job.result;

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
        const studyBtnColor = (study.status === 'NOT_STARTED' || study.status === 'IN_PROGRESS')
                                    ? 'primary'
                                    : 'white';

        // 요소 세팅
        if (!$existingCard.length) return;
        const content = `
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
                    <a href="/study/${study.dailyStudyNo}" class="btn primary ${studyBtnColor}">${studyBtnText}</a>
                    <a href="/study/${study.dailyStudyNo}/essay" class="btn ${isNotEssay ? 'full_blue highlight' : 'blue'}">
                        AI 피드백 기반 논술형 문제 풀기
                    </a>
                </div>
            </div>
        `;
        
        $existingCard.html(content); // 해당 요소에 컨텐츠 추가
        if (index !== 0) $existingCard.addClass("close"); // 첫 번째 요소가 아니라면 커서 닫힌 상태

        // 학습 상태에 따라 표현할 텍스트 설정
        // let statusText = '';
        // const tagList = [];
        // switch (study.status) {
        //     case 'NOT_STARTED':
        //         statusText = '오늘의 학습 시작하기';
        //         tagList.push({label: "학습 전", color: "gray"});
        //         break;
        //     case 'IN_PROGRESS':
        //         statusText = '지난 학습 이어하기';
        //         tagList.push({label: "학습 중", color: "red"});
        //         break;
        //     case 'COMPLETED':
        //         statusText = '복습하기';
        //         tagList.push({label: "학습 완료", color: "green"});
        //         break;
        // }

        // tagList를 HTML로 변환
        // const tagHtml = tagList
        // .map(tag => `<span class="study_tag ${tag.color}">${tag.label}</span>`)
        // .join('');

        // 서술형 퀴즈 완료 여부 체크
        // if (study.essayQuizCompleted) {
        //     tagList.push({label: "서술형 퀴즈 완료", color: "purple"});
        // } else {
        //     tagList.push({label: "서술형 퀴즈 미완료", color: "gray"});
        // }

        // 서술형 퀴즈 버튼을 강조하는 클래스를 설정하는 변수
        // 학습은 완료했지만 서술형퀴즈를 풀지 않은 경우에만 버튼 강조
        // const essayBtnClass = (study.status === 'COMPLETED'
        //     && !study.essayQuizCompleted) ?
        //     'highlight' : '';

        // index가 0일때만 표시할 버튼 html구성
        // const buttons = index === 0 ? `
        //                             <div class="btn_list column">
        //                               <a href="/study/${study.dailyStudyNo}" class="btn dark">${statusText}</a>
        //                               <a href="/study/${study.dailyStudyNo}/essay" class="btn white ${essayBtnClass}">AI 피드백 기반 논술형 퀴즈 풀기</a>
        //                             </div>
        //                           ` : ``;

        // 사실상 이 메소드가 호출될 땐 $existingCard가 0이 아니긴함 but 예외대비
        // if ($existingCard.length) {
        //     $existingCard.removeClass("skeleton")
        //     .attr("data-study-no", study.dailyStudyNo)
        //     .attr("data-study-status", statusText)
        //     .attr("data-essay-btn-class", essayBtnClass);
        //
        //     if (index === 0) {
        //         $existingCard.addClass("no_cursor");
        //     }
        //
        //     const innerHtml = `
        //         <div class="study_info">
        //             <div class="component_title">${study.title}</div>
        //             <div class="study_content">${study.explanation
        //         ? study.explanation : ''}</div>
        //             <div class="study_tag_list">${tagHtml}</div>
        //         </div>
        //         <div class="level_info study_list">
        //             <div class="bar_gauge">
        //                 <div class="gauge" style="width: ${study.progressRate}%"></div>
        //             </div>
        //         </div>
        //         ${buttons}
        //       `;
        //
        //     $existingCard.html(innerHtml);
        // }
    }
}

// export function renderStudyListCardFirstSkeleton(jobId, $studyListContainer) {
//     const skeletionHtml = `
//         <div class="component base_list_component daily_study_card"
//             data-job-id="${jobId}">
//             <div class="study_info">
//                 <div class="component_title skeleton _w70"></div>
//                 <div class="study_content d_flex column tiny_gap">
//                     <p class="skeleton"></p>
//                     <p class="skeleton _w70"></p>
//                 </div>
//                 <div class="study_tag_list">
//                     <div class="study_tag skeleton _w30"></div>
//                     <div class="study_tag skeleton _w30"></div>
//                 </div>
//                 <div class="bar_gauge skeleton"></div>
//                 <div class="btn skeleton"></div>
//             </div>
//         </div>
//     `;
//     $studyListContainer.append(skeletionHtml);
// }

// 스켈레톤 로딩 표시 세팅(유연하게 처리하기 위해 내용만 생성하여 반환/직접 세팅하지 않음)
export function renderStudyListCardFirstSkeleton(jobStatusList) {
    let content = '';

    for (const job of jobStatusList) {
        content += `
            <div class="component base_list_component daily_study_card"
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
            </div>
        `;
    }

    return content;
}