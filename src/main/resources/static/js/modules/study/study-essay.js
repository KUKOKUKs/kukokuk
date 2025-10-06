import {
    apiCreateEssayQuizLog,
    apiRequestEssayFeedback,
    apiUpdateEssayQuizLog
} from "./study-api.js";

const $answerInput = $('#essay-answer-input');
const $aiFeedbackContainer = $('#essay-feedback-box-container');

$(document).ready(function() {
    if(essayQuizLogAiFeedback != null){
        const feedbackData = (typeof essayQuizLogAiFeedback === 'string')
            ? JSON.parse(essayQuizLogAiFeedback)
            : essayQuizLogAiFeedback;

        $('.content').removeClass('center').addClass('show_essay_feedback');
        renderAiFeedback(feedbackData);
    }
});

/**
 * AI 피드백 버튼을 눌렀을 때 동작하는 이벤트 핸들러 메소드
 */
$('#essay-feedback-btn').click(async function() {

    const userAnswer = $answerInput.val().trim();
    if (!userAnswer) {
        alert('답변을 입력해주세요.')
        return;
    }

    // 오른쪽 피드백 창이 열리도록 설정
    $('.content').removeClass('center').addClass('show_essay_feedback');

    // 응답받기 전까지 로딩표시 추가
    $aiFeedbackContainer.html(`
            <div class="loading_spinner">
                <div class="info_text">AI 피드백 생성 중...</div>
            </div>
        `);

    try {
        // dailyStudyEssayQuizLogNo가 null이 아니라면 요청바디에 포함
        // null이면 포함하지않고 AI요청
        const data = await apiRequestEssayFeedback(essayQuizLogNo, essayQuizNo, userAnswer);
        renderAiFeedback(data);
    } catch (err) {
        alert('피드백 생성에 실패했어요, 다시 시도해주세요');
        $aiFeedbackContainer.html(`<div class="info_text">피드백 생성에 실패했어요, 다시 시도해주세요.</div>`);
    }
});


/**
 * 저장하기 버튼을 눌렀을 때 동작하는 이벤트 핸들러 메소드
 */
$('#essay-save-btn').click(async function() {
    const userAnswer = $answerInput.val().trim();
    if (!userAnswer) {
        alert('답변을 입력해주세요.')
        return;
    }

    try {
        let res;
        // essayQuizLogNo가 null이 아니면 서술형퀴즈 이력 수정 요청 호출
        if (essayQuizLogNo != null) {
            res = await apiUpdateEssayQuizLog(essayQuizLogNo, essayQuizNo, userAnswer);
        }
        // essayQuizLogNo가 null이면 서술형퀴즈 이력 생성 요청 호출
        else {
            res = await apiCreateEssayQuizLog(essayQuizNo, userAnswer);
            essayQuizLogNo = res.dailyStudyEssayQuizLogNo; // 새로 생성된 로그 번호 갱신
        }
        alert('저장되었습니다');
        console.log(res);
    } catch (err) {
        alert('저장에 실패했습니다');
    }
});

/**
 * AI 피드백을 렌더링하는 함수
 * @param data
 * data 형식
 * "sections": [
 *       {
 *         "type": "summary",
 *         "title": "총평",
 *         "items": [
 *           {
 *             "extra": {"icon": '👍'},
 *             "text": "답변이 매우 짧고, 문제에서 요구하는 내용을 거의 포함하지 못했습니다. '에브리씽 에브리원 올 앳 원스'라는 좋아하는 영화를 제시했지만, 등장인물, 사건, 시간 순서대로 정리하는 방법을 설명하는 데 실패했습니다. 영화의 순서를 모른다는 점을 솔직하게 밝혔지만, 문제 해결을 위한 추가적인 노력이 필요합니다."
 *           }
 *         ]
 *       },
 *     ]
 */
function renderAiFeedback(data) {
    $aiFeedbackContainer.empty();

    data.sections.forEach((section, idx) => {
        let itemHtml = '';
        section.items.forEach(item => {
            if(item.text != null) {
                itemHtml += `<div>
                        ${item.text}
                    </div>`;
            }
        })

        const sectionHtml = `
                <div class="component essay_feedback_box">
                    <div class="title">
                        <span>${section.icon ? section.icon : ''} </span>
                        ${section.title}
                    </div>
                    ${itemHtml}
                </div>
            `;

        // 문자열을 jQuery 객체로 변환
        const $sectionEl = $(sectionHtml);

        // CSS 변수 --delay로 카드별 딜레이 지정 (0s, 1s, 2s...)
        $sectionEl.css('--delay', `${idx * 1}s`);

        // DOM에 추가
        $aiFeedbackContainer.append($sectionEl);

        // 강제 리플로우
        // jQuery객체에서 [0]은 순수 DOM Element
        // .offsetHeight는 DOM요소의 렌더링된 높이를 픽셀단위로 반환
        // 여기서 값을 읽는 것 자체로 지금까지의 모든 스타일 계산과 레이아웃 작업이 완료됨
        // 브라우저가 reveal이 반영되지 않은 초기상태의 요소를 화면에 먼저 반영하도록 함
        $sectionEl[0].offsetHeight;

        // 클래스 부여로 애니메이션 시작
        $sectionEl.addClass('reveal');
    })
}