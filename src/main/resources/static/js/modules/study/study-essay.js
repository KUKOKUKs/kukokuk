import {
    apiCreateEssayQuizLog,
    apiRequestEssayFeedback,
    apiUpdateEssayQuizLog
} from "./study-api.js";

$(document).ready(function() {
    // 논술형 AI 피드백 관련
    const $answerInput = $('#essay-answer-input'); // 사용자 답변
    const $essayFeedbackBtn = $("#essay-feedback-btn"); // AI 피드백 요청 버튼
    const $essaySaveBtn = $('#essay-save-btn'); // 임시저장 버튼
    const $essayFeedbackContainer = $('.essay_feedback_container'); // 피드백 컨테이너 요소(숨어져 있음)
    const $essayFeedbackList = $("#essay-feedback-list"); // 피드백이 추가될 요소

    // AI 피드백 버튼 이벤트 핸들러
    $essayFeedbackBtn.click(async function() {
        const essayAnswer = $answerInput.val().trim();

        // 유효성 검사
        if (!essayAnswer) {
            alert("답변을 입력해 주세요.");
            $answerInput.focus();
            return false;
        }
        await getAiFeedbackProgress(essayAnswer); // api 요청 및 랜더링
    });

    // 저장하기 버튼을 눌렀을 때 동작하는 이벤트 핸들러
    $essaySaveBtn.click(async function() {
        const essayAnswer = $answerInput.val().trim();
        const essayQuizNo = $essayFeedbackContainer.data("quiz-no");
        const essayQuizLogNo = $essayFeedbackContainer.data("quiz-log-no") || null;

        // 유효성 검사
        if (!essayAnswer) {
            alert('답변을 입력해 주세요.');
            $answerInput.focus();
            return false;
        }

        try {
            let res;
            // essayQuizLogNo가 null이 아니면 서술형퀴즈 이력 수정 요청 호출
            if (essayQuizLogNo != null) {
                res = await apiUpdateEssayQuizLog(essayQuizLogNo, essayQuizNo, essayAnswer);
            } else {
                // essayQuizLogNo가 null이면 서술형퀴즈 이력 생성 요청 호출
                res = await apiCreateEssayQuizLog(essayQuizNo, essayAnswer);
                $essayFeedbackContainer.data("quiz-log-no", res.dailyStudyEssayQuizLogNo);
            }
            alert('저장되었습니다');
            console.log(res);
        } catch (err) {
            alert('저장에 실패했습니다');
        }
    });
    
    // AI 피드백 api 요청 및 랜더링 작업 함수
    async function getAiFeedbackProgress(essayAnswer) {
        const essayQuizNo = $essayFeedbackContainer.data("quiz-no");
        const essayQuizLogNo = $essayFeedbackContainer.data("quiz-log-no") || null;

        $essayFeedbackContainer.removeClass("close"); // 피드백 컨테이너 요소 노출

        // 로딩
        $essayFeedbackList.html(`
            <div class="component">
                <div class="loading_spinner">
                    <div class="info_text">AI 피드백 생성 중...</div>
                </div>
            </div>
        `);

        try {
            // dailyStudyEssayQuizLogNo가 null이 아니라면 요청바디에 포함
            // null이면 포함하지않고 AI요청
            const aiFeedBackJson = await apiRequestEssayFeedback(essayQuizLogNo, essayQuizNo, essayAnswer);
            renderAiFeedback(aiFeedBackJson);
        } catch (err) {
            alert('피드백 생성에 실패했어요, 다시 시도해주세요');
            $essayFeedbackList.html(`
                <div class="component">
                    <div class="info_text">피드백 생성에 실패했어요, 다시 시도해주세요.</div>
                </div>
            `);
        }
    }

    /**
     * AI 피드백을 렌더링하는 함수
     * @param jsonData
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
    function renderAiFeedback(jsonData) {
        $essayFeedbackList.empty();
        jsonData.sections.forEach((section, idx) => {
            let itemHtml = '';
            section.items.forEach(item => {
                if(item.text != null) itemHtml += `${item.text} `;
            })

            const sectionHtml = `
                <div class="component small_list_component essay_feedback_box">
                    <div class="component_title">
                        <p class="title_info">
                            ${section.icon ? `${section.icon} ${section.title}` : section.title}
                        </p>
                    </div>
                    
                    <div class="component_info w_100">${itemHtml}</div>
                </div>
            `;

            // 문자열을 jQuery 객체로 변환
            const $sectionEl = $(sectionHtml);

            // CSS 변수 --delay로 카드별 딜레이 지정 (0s, 1s, 2s...)
            $sectionEl.css('--delay', `${idx * 1}s`);

            // DOM에 추가
            $essayFeedbackList.append($sectionEl);

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

    // 진행 중단 모달창 열기
    const $progressEndBtn = $('.progress_end_btn');
    const $modalEssayExit = $('#modal-essay-exit');
    $progressEndBtn.click(function() {
        if ($modalEssayExit.length) {
            // 해당 모달창 요소가 있을 경우 열기
            $modalEssayExit.show();

            // 약간의 딜레이를 주어 show 후 css transition 적용될 수 있도록 함
            setTimeout(() => {
                $modalEssayExit.addClass("open");
            }, 10);
        }
    });
});