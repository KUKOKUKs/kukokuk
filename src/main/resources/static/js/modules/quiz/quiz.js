import {apiUseHint} from "./quiz-api.js";
import {
    createResultPayload,
    getRandomWrongOption,
    goToNext,
    initializeQuizState,
    quizState,
    recordHintUsage,
    selectAnswer
} from "./quiz-handler.js";

/**
 * 통합 퀴즈 UI 및 이벤트 처리
 * Speed 퀴즈와 Level 퀴즈의 UI 렌더링, DOM 조작, 이벤트 리스너를 담당합니다.
 * quiz-api.js와 quiz-handler.js에 의존합니다.
 */
$(document).ready(function () {
    // --- 유효성 검사 ---
    if (typeof quizMode === 'undefined') {
        console.error("quizMode 변수가 정의되지 않았습니다. HTML에서 quizMode를 선언해주세요.");
        return;
    }
    if (typeof quizzes === 'undefined' || !Array.isArray(quizzes) || quizzes.length === 0) {
        console.error("퀴즈 데이터를 불러오지 못했습니다.");
        return;
    }

    // --- 전역 변수 및 상수 ---
    let timerInterval = null;
    let timeLeft = 10;
    const isSpeedMode = quizMode === "speed";
    const isLevelMode = quizMode === "level";

    // --- DOM 요소 캐싱 ---
    const $timerContainer = $(".progress_container");
    const $quizCurrent = $("#quiz-current");
    const $quizQuestion = $("#quiz-question");
    const $quizCallout = $("#quiz-callout");
    const $quizOptions = $("#quiz-options");
    const $quizTimer = $("#quiz-timer");
    const $timerBar = $("#timer-progress-bar");
    const $hintBtn = $("#hint-btn");
    const $hintCount = $("#hint-count");
    const $progressBar = $("#step-progress-bar");
    const $prevBtn = $("#prev-btn"); // Level 모드에서 숨김 처리
    const $nextBtn = $("#next-btn");

    // --- 초기화 ---
    initializeQuizState(quizzes.length);
    if (isLevelMode) {
        $prevBtn.hide();
    }
    renderCurrentQuiz();

    // --- 함수 선언 ---

    /**
     * 현재 퀴즈를 화면에 렌더링합니다.
     */
    function renderCurrentQuiz() {
        const idx = quizState.currentQuizIndex;
        const quiz = quizzes[idx];

        $quizCurrent.text(idx + 1);
        if ($quizCallout.length) {
            $quizCallout.text(quiz.questionType);
        }
        $quizQuestion.text(quiz.question);
        $quizOptions.empty();

        let optionsHtml = "";
        quiz.options.forEach((optionText, i) => {
            const choiceNum = i + 1;
            const isDisabled = isLevelMode && quiz.hintRemovedOption === choiceNum;
            const isSelected = quizState.selectedAnswers[idx] === choiceNum;

            optionsHtml += `
                <button type="button" class="component_info option_btns ${isDisabled ? 'hint-removed' : ''} ${isSelected ? 'selected' : ''}" 
                        data-choice="${choiceNum}" ${isDisabled ? 'disabled' : ''}>
                    <div class="list_option">
                        <span class="marker">${choiceNum}.</span>
                        <p class="option">${optionText}</p>
                    </div>
                </button>`;
        });
        $quizOptions.append(optionsHtml);

        if (isLevelMode) {
            updateHintButton();
            updateNavigationButtons();
            renderProgressBar();
        } else if (isSpeedMode) {
            startTimer();
        }
    }

    /**
     * Speed 모드의 타이머를 시작합니다.
     */
    function startTimer() {
        clearInterval(timerInterval);
        timeLeft = 10;
        $timerContainer.removeClass("warning");
        $quizTimer.text(timeLeft + " 초");
        $timerBar.stop(true, true).css({ width: "100%" }).animate({ width: 0 }, timeLeft * 1000, "linear");

        timerInterval = setInterval(() => {
            timeLeft--;
            if (timeLeft <= 3) $timerContainer.addClass("warning");
            $quizTimer.text((timeLeft < 10 ? "0" + timeLeft : timeLeft) + " 초");
            if (timeLeft <= 0) {
                clearInterval(timerInterval);
                $quizOptions.find(".option_btns").addClass("disabled");
                handleSubmit();
            }
        }, 1000);
    }

    /**
     * Speed 모드에서 답안을 자동으로 제출 처리합니다.
     */
    function handleSubmit() {
        clearInterval(timerInterval);

        if (quizState.currentQuizIndex < quizzes.length - 1) {
            goToNext();
            renderCurrentQuiz();
        } else {
            submitResults();
        }
    }

    /**
     * Level 모드의 힌트 버튼 상태를 업데이트합니다.
     */
    function updateHintButton() {
        if (!$hintBtn.length) return;
        const userHintCount = parseInt($hintCount.text());
        const isHintUsedForCurrent = quizState.usedHints[quizState.currentQuizIndex];

        if (userHintCount <= 0 || isHintUsedForCurrent) {
            $hintBtn.addClass("disabled").prop("disabled", true);
        } else {
            $hintBtn.removeClass("disabled").prop("disabled", false);
        }
    }

    /**
     * 지정된 보기를 비활성화 처리합니다.
     * @param {number} optionNumber - 비활성화할 보기 번호 (1-based)
     */
    function disableOption(optionNumber) {
        const $optionBtn = $quizOptions.find(`button[data-choice="${optionNumber}"]`);
        $optionBtn.addClass("hint-removed").prop("disabled", true).off("click");
        $optionBtn.find(".option").css({ "text-decoration": "line-through", "opacity": "0.5", "color": "#999" });
        if ($optionBtn.hasClass("selected")) {
            $optionBtn.removeClass("selected");
            selectAnswer(undefined); // 선택 해제
        }
    }

    /**
     * Level 모드의 진행률 바를 업데이트합니다.
     */
    function renderProgressBar() {
        if (!$progressBar.length) return;
        const progress = ((quizState.currentQuizIndex + 1) / quizzes.length) * 100;
        $progressBar.css("width", progress + "%");
    }

    /**
     * Level 모드의 네비게이션 버튼(다음/제출)을 업데이트합니다.
     */
    function updateNavigationButtons() {
        if (!$nextBtn.length) return;
        const isLastQuiz = quizState.currentQuizIndex === quizzes.length - 1;
        $nextBtn.text(isLastQuiz ? "제출" : "다음");
    }

    /**
     * 퀴즈 결과를 서버에 제출합니다.
     */
    function submitResults() {
        console.log("퀴즈 완료! 결과 폼 제출");
        const payload = createResultPayload(quizMode, quizzes);
        const $form = $("#quiz-result-form").empty();

        // CSRF 토큰 추가
        const csrfToken = $("meta[name='_csrf']").attr("content");
        if (csrfToken) {
            $form.append(`<input type="hidden" name="_csrf" value="${csrfToken}">`);
        }

        $form.append(`<input type="hidden" name="totalTimeSec" value="${payload.totalTimeSec}">`);
        $form.append(`<input type="hidden" name="quizMode" value="${payload.quizMode}">`);

        payload.results.forEach((r, idx) => {
            for (const key in r) {
                $form.append(`<input type="hidden" name="results[${idx}].${key}" value="${r[key]}">`);
            }
        });

        $form.submit();
    }

    // --- 이벤트 리스너 ---

    // 보기 선택 이벤트
    $(document).on("click", ".option_btns:not(.hint-removed):not(.disabled)", function () {
        const choiceNumber = Number($(this).data("choice"));
        selectAnswer(choiceNumber);

        $quizOptions.find(".option_btns").removeClass("selected");
        $(this).addClass("selected");

        if (isSpeedMode) {
            $quizOptions.find(".option_btns").addClass("disabled");
            handleSubmit();
        }
    });

    // 힌트 버튼 클릭 이벤트 (Level 모드)
    $hintBtn.on("click", async function () {
        if ($(this).hasClass("disabled") || !isLevelMode) return;
        if (parseInt($hintCount.text()) <= 0 || quizState.usedHints[quizState.currentQuizIndex]) return;
        if (!confirm("힌트를 사용하시겠습니까? (1개 차감)")) return;

        $(this).addClass("disabled").prop("disabled", true);

        const currentQuiz = quizzes[quizState.currentQuizIndex];
        const removedOption = getRandomWrongOption(currentQuiz);

        disableOption(removedOption);
        recordHintUsage();
        currentQuiz.hintRemovedOption = removedOption; // API 전송 및 렌더링을 위해 quiz 객체에 직접 기록

        try {
            const response = await apiUseHint(quizState.currentQuizIndex, removedOption);
            if (response.success && response.data !== undefined) {
                $hintCount.text(response.data);
            }
        } catch (e) {
            alert("힌트 사용에 실패했습니다. 다시 시도해주세요.");
            // 실패 시 UI 원복 또는 재시도 로직이 필요할 수 있으나, 우선 경고만 표시
            $(this).removeClass("disabled").prop("disabled", false);
        } finally {
            $hintCount.addClass("action");
            setTimeout(() => $hintCount.removeClass("action"), 200);
        }
    });

    // 다음 버튼 클릭 이벤트 (Level 모드)
    $nextBtn.on("click", function () {
        if ($(this).hasClass("disabled") || !isLevelMode) return;
        if (quizState.selectedAnswers[quizState.currentQuizIndex] === undefined) {
            alert("답을 선택해주세요.");
            return;
        }

        const isLastQuiz = quizState.currentQuizIndex === quizzes.length - 1;
        if (isLastQuiz) {
            $(this).addClass("disabled");
            submitResults();
        } else {
            if(quizState.currentQuizIndex < quizzes.length - 1) {
                goToNext();
                renderCurrentQuiz();
            }
        }
    });

    // 퀴즈 종료 모달창
    $(".quiz_end_btn").on("click", function () {
        const $modal = $("#modal-quiz-exit");
        if ($modal.length) {
            $modal.show();
            setTimeout(() => $modal.addClass("open"), 10);
        }
    });
});

