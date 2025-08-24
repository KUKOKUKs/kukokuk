/**
 * 주어진 학습(study) 데이터를 기반으로 학습자료 목록에 표시할 카드 HTML을 생성하는 함수
 * @param {Object} study - 서버에서 내려받은 학습 객체 {
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
 * @param index 학습 카드의 인덱스 (목록 순서)
 * @returns {string} 완성된 학습 카드의 HTML 문자열
 */
export function renderStudyListCard(study, index) {
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
                                <div class="study_btn_list">
                                  <a href="/study/${study.dailyStudyNo}" class="btn dark">${statusText}</a>
                                  <a href="/study/${study.dailyStudyNo}/essay" class="btn white ${essayBtnClass}">AI 피드백 기반 논술형 퀴즈 풀기</a>
                                </div>
                              ` : ``;

    // 최종적으로 조립된 학습 카드 HTML 반환
    return  `<div class="daily_study_card ${index === 0 ? 'no_cursor' : ''}"
                data-study-no="${study.dailyStudyNo}"
                data-study-status="${statusText}"
                data-essay-btn-class="${essayBtnClass}">
                <div class="study_info">
                    <div class="study_title">${study.title}</div>
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


