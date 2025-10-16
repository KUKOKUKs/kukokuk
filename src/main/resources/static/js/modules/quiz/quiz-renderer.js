/**
 * 퀴즈 문제 렌더링
 * @param {Object} quiz - 퀴즈 객체
 * @param {number} currentIndex - 현재 퀴즈 인덱스
 * @param {number} totalQuizzes - 전체 퀴즈 개수
 */
export function renderQuizQuestion(quiz, currentIndex, totalQuizzes) {
    console.log("renderQuizQuestion() 실행");

    $("#quiz-current").text(currentIndex + 1);
    $("#quiz-total").text(totalQuizzes);
    $("#quiz-question").text(quiz.question);

    if ($("#quiz-callout").length) {
        $("#quiz-callout").text(quiz.questionType);
    }
}

/**
 * 퀴즈 보기 렌더링
 * @param {Object} quiz - 퀴즈 객체
 * @param {number} selectedAnswer - 선택된 답안
 * @param {boolean} isHintUsed - 힌트 사용 여부
 */
export function renderQuizOptions(quiz, selectedAnswer = null, isHintUsed = false) {
    console.log("renderQuizOptions() 실행");

    const $quizOptions = $("#quiz-options");
    $quizOptions.empty();

    let optionsHtml = "";

    for (let i = 1; i <= quiz.options.length; i++) {
        const isDisabled = isHintUsed && quiz.hintRemovedOption === i;
        const isSelected = selectedAnswer === i;
        const disabledClass = isDisabled ? "hint-removed" : "";
        const selectedClass = isSelected ? "selected" : "";

        optionsHtml += `
            <button type="button" 
                    class="component_info option_btns ${disabledClass} ${selectedClass}" 
                    data-choice="${i}" 
                    ${isDisabled ? 'disabled' : ''}>
                <div class="list_option">
                    <span class="marker">${i}.</span>
                    <p class="option">${quiz.options[i - 1]}</p>
                </div>
            </button>
        `;
    }

    $quizOptions.html(optionsHtml);
}

/**
 * 진행률 바 업데이트 (Level 모드)
 * @param {number} currentIndex - 현재 퀴즈 인덱스
 * @param {number} totalQuizzes - 전체 퀴즈 개수
 */
export function renderProgressBar(currentIndex, totalQuizzes) {
    console.log("renderProgressBar() 실행");

    const $progressBar = $("#step-progress-bar");
    if (!$progressBar.length) return;

    const progressPercent = ((currentIndex + 1) / totalQuizzes) * 100;
    $progressBar.css("width", progressPercent + "%");
}

/**
 * 타이머 UI 업데이트 (Speed 모드)
 * @param {number} timeLeft - 남은 시간
 * @param {number} totalTime - 전체 시간
 */
export function renderTimer(timeLeft, totalTime) {
    const $timerContainer = $(".progress_container");
    const $quizTimer = $("#quiz-timer");

    // 3초 이하일 때 경고 표시
    if (timeLeft <= 3) {
        $timerContainer.addClass("warning");
    } else {
        $timerContainer.removeClass("warning");
    }

    const displayTime = timeLeft < 10 ? "0" + timeLeft : timeLeft;
    $quizTimer.text(displayTime + " 초");
}

/**
 * 타이머 진행바 애니메이션 시작
 * @param {number} duration - 애니메이션 지속 시간(초)
 */
export function startTimerAnimation(duration) {
    const $timerBar = $("#timer-progress-bar");

    $timerBar.stop(true, true)
    .css({width: "100%"})
    .animate({width: 0}, duration * 1000, "linear");
}

/**
 * 힌트 버튼 상태 업데이트
 * @param {number} hintCount - 남은 힌트 개수
 * @param {boolean} isUsed - 현재 문제에서 힌트 사용 여부
 */
export function renderHintButton(hintCount, isUsed) {
    console.log("renderHintButton() 실행");

    const $hintBtn = $("#hint-btn");
    const $hintCount = $("#hint-count");

    if (!$hintBtn.length) return;

    $hintCount.text(hintCount);

    if (hintCount <= 0 || isUsed) {
        $hintBtn.addClass("disabled").prop("disabled", true);
    } else {
        $hintBtn.removeClass("disabled").prop("disabled", false);
    }
}

/**
 * 네비게이션 버튼 상태 업데이트
 * @param {number} currentIndex - 현재 퀴즈 인덱스
 * @param {number} totalQuizzes - 전체 퀴즈 개수
 */
export function renderNavigationButtons(currentIndex, totalQuizzes) {
    console.log("renderNavigationButtons() 실행");

    const $prevBtn = $("#prev-btn");
    const $nextBtn = $("#next-btn");

    if ($prevBtn.length) {
        $prevBtn.prop("disabled", currentIndex === 0);
    }

    if ($nextBtn.length) {
        const isLastQuestion = currentIndex === totalQuizzes - 1;
        $nextBtn.text(isLastQuestion ? "제출" : "다음");
    }
}