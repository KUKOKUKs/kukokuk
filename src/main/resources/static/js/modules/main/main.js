import {apiGetDailyStudies} from "../study/study-api.js";

$(document).ready(async () => {
    const $studyListContainer = $('.study_list_container');

    await renderDailyStudy();

    async function renderDailyStudy(username) {
        const studyList = await apiGetDailyStudies(1);

        studyList.forEach((study, index) => {
            // 학습 상태에 따라 표현할 텍스트 설정
            let statusText = '';
            const tagList = [];
            switch (study.status) {
                case 'NOT_STARTED':
                    statusText = '오늘의 학습 시작하기';
                    tagList.push({label: "학습 전", color: "gray"});
                    break;
                case 'IN_PROGRESS':
                    statusText = '지난 학습 이어하기';
                    tagList.push({label: "학습 중", color: "red"});
                    break;
                case 'COMPLETED':
                    statusText = '복습하기';
                    tagList.push({label: "학습 완료", color: "green"});
                    break;
            }

            // 서술형 퀴즈 완료 여부 체크
            if (study.essayQuizCompleted) {
                tagList.push({label: "서술형 퀴즈 완료", color: "purple"});
            } else {
                tagList.push({label: "서술형 퀴즈 미완료", color: "gray"});
            }

            // tagList를 HTML로 변환
            const tagHtml = tagList
            .map(
                tag => `<span class="study_tag ${tag.color}">${tag.label}</span>`)
            .join('');

            // 서술형 퀴즈 버튼을 강조하는 클래스를 설정하는 변수
            // 학습은 완료했지만 서술형퀴즈를 풀지 않은 경우에만 버튼 강조
            const essayBtnClass = (study.status === 'COMPLETED'
                && !study.essayQuizCompleted) ?
                'highlight' : '';

            const buttons = `
                                <div class="study_btn_list">
                                  <a href="/study/${study.dailyStudyNo}" class="btn dark">${statusText}</a>
                                  <a href="/study/${study.dailyStudyNo}/essay" class="btn white ${essayBtnClass}">AI 피드백 기반 논술형 퀴즈 풀기</a>
                                </div>
                              `;

            const studyContent = `
                                <div class="daily_study_card no_cursor"
                                    data-study-no="${study.dailyStudyNo}"
                                    data-study-status="${statusText}"
                                    data-essay-btn-class="${essayBtnClass}">
                                    <div class="study_info">
                                        <div class="study_title">${study.title}</div>
                                        <div class="study_content">${study.explanation
                ? study.explanation : ''}</div>
                                        <div class="study_tag_list">${tagHtml}</div>
                                    </div>
                                    <div class="level_info study_list">
                                        <div class="level_bar">
                                            <div class="experience_point" style="width: ${study.progressRate}%"></div>
                                        </div>
                                    </div>
                                    ${buttons}
                                </div>
                              `;

            $studyListContainer.append(studyContent);
        });
    }
})