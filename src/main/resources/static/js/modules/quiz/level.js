/*
    스피드 퀴즈와 반복되는 구조와 로직이라면 
    하나로 처리하는게 가능해 보임
    따로 관리하는편이 맞아 보이지만 같은 컨텐츠라고 봐도 무방하기에
    스피드, 단계별 퀴즈에 대한 로직은 하나로 통합이 오히려 효율적일 것 같음
    (구분을 위한 값이 필요 현재 타입으로 구분이 가능해 보임)
 */
$(document).ready(function () {
    // 현재 퀴즈 인덱스 (0부터 시작)
    let currentQuizIndex = 0;

    // 사용자가 선택한 보기 번호를 저장하는 배열
    let selectedAnswers = [];

    // 각 문제별 힌트 사용 여부를 저장하는 배열
    let usedHints = [];

    // 자주 사용하는 DOM 요소 캐싱
    const $quizTotal = $("#quiz-total");
    const $quizCurrent = $("#quiz-current");
    const $quizQuestion = $("#quiz-question");
    const $quizOptions = $("#quiz-options");
    const $hintBtn = $("#hint-btn");
    const $hintCount = $("#hint-count");

    console.log("quizzes length =", quizzes.length); // 서버에서 전달된 퀴즈 데이터 확인용

    // 사용자가 선택한 답안을 저장할 배열 초기화
    selectedAnswers = new Array(quizzes.length).fill(null);

    // 각 문제별 힌트 사용 여부 초기화
    usedHints = new Array(quizzes.length).fill(false);

    // 총 문제 수 & 현재 문제 번호 표시
    $quizTotal.text(quizzes.length);
    $quizCurrent.text(1);

    // 첫 번째 문제 렌더링
    renderCurrentQuiz();

    /**
     * 현재 인덱스(currentQuizIndex)에 해당하는 문제와 보기를 화면에 출력
     */
    function renderCurrentQuiz() {
        const quiz = quizzes[currentQuizIndex];

        // 문제 번호와 질문 표시
        $quizCurrent.text(currentQuizIndex + 1);
        $quizQuestion
        .text(`Q${currentQuizIndex + 1}. ${quiz.question}`)
        .css("text-align", "center");

        // 이전 보기를 모두 비움
        $quizOptions.empty();

        // 보기 1~4 생성
        for (let i = 1; i <= 4; i++) {
            const optionText = quiz["option" + i]; // quiz.option1~4
            const isSelected = selectedAnswers[currentQuizIndex] === i; // 선택 여부 확인

            // 보기 버튼 생성
            const $btn = $(`
                <button class="btn white" type="button" data-choice="${i}">
                    <span class="choice-no">${i}.</span>
                    <span class="choice-text">${optionText}</span>
                </button>
            `);

            // 이미 선택된 경우 selected 클래스 추가
            if (isSelected) {
                $btn.addClass("selected");
            }

            // 보기 클릭 시 선택 처리
            $btn.on("click", function () {
                // 비활성화된 보기는 클릭 불가
                if ($(this).hasClass('hint-removed')) {
                    return;
                }

                selectedAnswers[currentQuizIndex] = i; // 선택한 번호 저장
                $quizOptions.find("button").removeClass("selected"); // 기존 선택 해제
                $(this).addClass("selected"); // 새 선택 적용
            });

            // 보기 영역에 버튼 추가
            $quizOptions.append($btn);
        }

        // 힌트가 이미 사용된 문제라면 해당 보기 비활성화 처리
        if (usedHints[currentQuizIndex]) {
            const removedOption = quiz.hintRemovedOption;
            if (removedOption) {
                disableOption(removedOption);
            }
        }

        // 힌트 버튼 상태 업데이트
        updateHintButton();

        // 이전/다음 버튼 상태 및 텍스트 변경
        $("#prev-btn").prop("disabled", currentQuizIndex === 0);
        $("#next-btn").text(currentQuizIndex === quizzes.length - 1 ? "제출" : "다음");

        // 진행률 바 업데이트
        renderProgressBar();
    }

    /**
     * 힌트 버튼 상태 업데이트
     */
    function updateHintButton() {
        const userHintCount = parseInt($hintCount.text());
        const isHintUsed = usedHints[currentQuizIndex];

        // 힌트 개수가 0개이거나 이미 사용한 문제면 비활성화
        if (userHintCount <= 0 || isHintUsed) {
            $hintBtn.prop('disabled', true).addClass('disabled');
        } else {
            $hintBtn.prop('disabled', false).removeClass('disabled');
        }
    }

    /**
     * 정답이 아닌 보기 중 하나를 랜덤하게 선택
     * @param {Object} quiz - 현재 퀴즈 객체
     * @returns {number} 제거할 보기 번호 (1~4)
     */
    function getRandomWrongOption(quiz) {
        const correctAnswer = quiz.successAnswer; // 정답 번호
        const wrongOptions = []; // 오답 보기들

        // 정답이 아닌 보기 번호들 수집
        for (let i = 1; i <= 4; i++) {
            if (i !== correctAnswer) {
                wrongOptions.push(i);
            }
        }

        // 오답 보기 중 랜덤 선택
        const randomIndex = Math.floor(Math.random() * wrongOptions.length);
        return wrongOptions[randomIndex];
    }

    /**
     * 지정된 보기를 비활성화 표시
     * @param {number} optionNumber - 비활성화할 보기 번호 (1~4)
     */
    function disableOption(optionNumber) {
        const $optionBtn = $quizOptions.find(`button[data-choice="${optionNumber}"]`);

        // 비활성화 스타일 적용
        $optionBtn
        .addClass('hint-removed')
        .prop('disabled', true)
        .off('click'); // 기존 클릭 이벤트 제거

        // 시각적 표시 (취소선, 흐리게 등)
        $optionBtn.find('.choice-text').css({
            'text-decoration': 'line-through',
            'opacity': '0.5',
            'color': '#999'
        });

        // 힌트 사용 표시 추가
        $optionBtn.append('<span class="hint-marker">❌</span>');

        // 선택되어 있었다면 선택 해제
        if ($optionBtn.hasClass('selected')) {
            $optionBtn.removeClass('selected');
            selectedAnswers[currentQuizIndex] = null;
        }
    }

    /**
     * 힌트 사용 Ajax 호출 함수
     * @param {number} quizIndex - 퀴즈 인덱스
     * @param {number} removedOption - 제거된 보기 번호
     */
    async function useHint(quizIndex, removedOption) {
        try {
            const response = await $.ajax({
                url: "/quiz/use-hint",
                method: "POST",
                data: {
                    quizIndex: quizIndex,
                    removedOption: removedOption
                },
                dataType: "json"
            });

            console.log("힌트 사용 반영 완료", response);

            // 서버에서 반환된 남은 힌트 개수로 UI 업데이트
            if (response.success && response.data) {
                const remainingHints = response.data.remainingHints || response.data;
                $hintCount.text(remainingHints).addClass("action");
            } else {
                // 기존 방식으로 차감 (서버 응답이 없을 경우)
                const newHintCount = parseInt($hintCount.text()) - 1;
                $hintCount.text(newHintCount).addClass("action");
            }

            // 일정 시간 후 액션 효과 제거
            setTimeout(() => $hintCount.removeClass("action"), 200);

        } catch (e) {
            console.error("힌트 사용 반영 실패", e);
            alert("힌트 사용에 실패했습니다. 다시 시도해주세요.");
        }
    }

    /**
     * 진행률 바의 너비를 현재 문제 번호에 맞게 업데이트
     */
    function renderProgressBar() {
        const $experiencePoint = $('#step-progress-bar'); // 진행률 바 내부 채움 영역
        const progressPercent = ((currentQuizIndex + 1) / quizzes.length) * 100;
        $experiencePoint.css('width', progressPercent + '%');
    }

    /**
     * 이전 버튼 클릭 이벤트
     */
    $("#prev-btn").off("click").on("click", function () {
        if (currentQuizIndex > 0) {
            currentQuizIndex--;
            renderCurrentQuiz();
        }
    });

    /**
     * 다음 버튼 클릭 이벤트
     * - 마지막 문제일 경우 제출 확인 후 결과 전송
     */
    $("#next-btn").off("click").on("click", function () {
        if (currentQuizIndex < quizzes.length - 1) {
            currentQuizIndex++;
            renderCurrentQuiz();
        } else {
            if (confirm("퀴즈를 제출하시겠습니까?")) {
                submitResults();
            }
        }
    });

    /**
     * 힌트 버튼 클릭 이벤트
     * - 정답이 아닌 보기 중 하나를 랜덤하게 비활성화
     */
    $hintBtn.off("click").on("click", function () {
        const userHintCount = parseInt($hintCount.text());

        // 힌트 개수 체크
        if (userHintCount <= 0) {
            alert("사용 가능한 힌트가 없습니다.");
            return;
        }

        // 이미 힌트를 사용한 문제인지 체크
        if (usedHints[currentQuizIndex]) {
            alert("이미 힌트를 사용한 문제입니다.");
            return;
        }

        // 확인 창 표시
        if (!confirm("힌트를 사용하시겠습니까? (1개 차감)")) {
            return;
        }

        const currentQuiz = quizzes[currentQuizIndex];

        // 정답이 아닌 보기 중 랜덤 선택
        const removedOption = getRandomWrongOption(currentQuiz);

        // 보기 비활성화
        disableOption(removedOption);

        // 힌트 사용 표시
        usedHints[currentQuizIndex] = true;
        currentQuiz.hintRemovedOption = removedOption; // 문제에 제거된 보기 정보 저장

        // 힌트 버튼 비활성화
        updateHintButton();

        // 서버에 힌트 사용 정보 전송
        useHint(currentQuizIndex, removedOption);
    });

    /**
     * 결과 제출 처리
     * - 숨겨진 폼 필드에 각 문제의 결과 데이터를 추가하고 서버로 전송
     */
    function submitResults() {
        const $form = $("#quiz-result-form");
        $form.empty();

        // 문제별 결과 데이터 추가
        quizzes.forEach((quiz, idx) => {
            $form.append($("<input>", {
                type: "hidden",
                name: `results[${idx}].quizNo`,
                value: quiz.quizNo
            }));
            $form.append($("<input>", {
                type: "hidden",
                name: `results[${idx}].selectedChoice`,
                value: selectedAnswers[idx] ?? 0
            }));
            $form.append($("<input>", {
                type: "hidden",
                name: `results[${idx}].isBookmarked`,
                value: "N"
            }));
            $form.append($("<input>", {
                type: "hidden",
                name: `results[${idx}].usedHint`,
                value: usedHints[idx] ? "Y" : "N"
            }));
        });

        // 퀴즈 모드와 시간 데이터
        $form.append($("<input>", {
            type: "hidden",
            name: "quizMode",
            value: "level"
        }));
        $form.append($("<input>", {
            type: "hidden",
            name: "totalTimeSec",
            value: "0.0"
        }));

        // CSRF 토큰 추가
        const csrfToken = $("meta[name='_csrf']").attr("content");
        if (csrfToken) {
            $form.append($("<input>", {
                type: "hidden",
                name: "_csrf",
                value: csrfToken
            }));
        }

        // 폼 전송
        $form.submit();
    }

    // 퀴즈 진행 종료 안내 모달창 열기
    const $quizEndBtn = $(".quiz_end_btn"); // 모달창 열기 버튼
    const $modalQuizExit = $("#modal-quiz-exit"); // 모달창
    $quizEndBtn.click(function () {
        if ($modalQuizExit.length) {
            // 해당 모달창 요소가 있을 경우 열기
            $modalQuizExit.show();

            // 약간의 딜레이를 주어 show 후 css transition 적용될 수 있도록 함
            setTimeout(() => {
                $modalQuizExit.addClass("open");
            }, 10);
        }
    });
});