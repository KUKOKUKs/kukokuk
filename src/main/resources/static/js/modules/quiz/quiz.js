/**
 * KUKOKUK 퀴즈 통합 모듈
 * 스피드 퀴즈와 단계별 퀴즈를 하나의 파일로 관리
 */

$(document).ready(function () {
    const $hintBtn = $("#hint-btn, #quizHint");
    const $quizTimer = $("#quiz-timer");

    const isSpeedMode = $quizTimer.length > 0;
    const isLevelMode = $hintBtn.length > 0 || $("#step-progress-bar").length > 0;

    if (isSpeedMode) {
        initSpeedQuiz();
    } else if (isLevelMode) {
        initLevelQuiz();
    }

    initQuizExitModal();
});

let currentQuizIndex = 0;
let selectedAnswers = [];
let usedHints = [];

const $quizTotal = $("#quiz-total");
const $quizCurrent = $("#quiz-current");
const $quizQuestion = $("#quiz-question");
const $quizOptions = $("#quiz-options");

function initCommon() {
    console.log("quizzes length =", quizzes.length);
    selectedAnswers = new Array(quizzes.length).fill(null);
    usedHints = new Array(quizzes.length).fill(false);
    $quizTotal.text(quizzes.length);
    $quizCurrent.text(1);
}

function renderQuizQuestion() {
    const quiz = quizzes[currentQuizIndex];
    $quizCurrent.text(currentQuizIndex + 1);
    $quizQuestion
    .text(`Q${currentQuizIndex + 1}. ${quiz.question}`)
    .css("text-align", "center");
}

function disableOption(optionNumber) {
    const $optionBtn = $quizOptions.find(`button[data-choice="${optionNumber}"]`);

    $optionBtn
    .addClass('hint-removed')
    .prop('disabled', true)
    .off('click');

    $optionBtn.find('.choice-text').css({
        'text-decoration': 'line-through',
        'opacity': '0.5',
        'color': '#999'
    });

    $optionBtn.append('<span class="hint-marker">X</span>');

    if ($optionBtn.hasClass('selected')) {
        $optionBtn.removeClass('selected');
        selectedAnswers[currentQuizIndex] = null;
    }
}

function submitResults(quizMode, additionalData = {}) {
    const $form = $("#quiz-result-form");
    $form.empty();

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

        if (quizMode === 'level') {
            $form.append($("<input>", {
                type: "hidden",
                name: `results[${idx}].usedHint`,
                value: usedHints[idx] ? "Y" : "N"
            }));
        }
    });

    $form.append($("<input>", {
        type: "hidden",
        name: "quizMode",
        value: quizMode
    }));

    Object.entries(additionalData).forEach(([key, value]) => {
        $form.append($("<input>", {
            type: "hidden",
            name: key,
            value: value
        }));
    });

    const csrfToken = $("meta[name='_csrf']").attr("content");
    if (csrfToken) {
        $form.append($("<input>", {
            type: "hidden",
            name: "_csrf",
            value: csrfToken
        }));
    }

    $form.submit();
}

function getRandomWrongOption(quiz) {
    const correctAnswer = quiz.successAnswer || quiz.success_answer;
    const wrongOptions = [];

    for (let i = 1; i <= 4; i++) {
        if (i !== correctAnswer) {
            wrongOptions.push(i);
        }
    }

    const randomIndex = Math.floor(Math.random() * wrongOptions.length);
    return wrongOptions[randomIndex];
}

function initLevelQuiz() {
    console.log("단계별 퀴즈 초기화");

    const $hintBtn = $("#hint-btn");
    const $hintCount = $("#hint-count");
    const $prevBtn = $("#prev-btn");
    const $nextBtn = $("#next-btn");
    const $progressBar = $('#step-progress-bar');

    initCommon();
    renderLevelQuiz();

    function renderLevelQuiz() {
        renderQuizQuestion();
        renderLevelQuizOptions();
        updateHintButton();
        updateNavigationButtons();
        updateProgressBar();
    }

    function renderLevelQuizOptions() {
        const quiz = quizzes[currentQuizIndex];
        $quizOptions.empty();

        for (let i = 1; i <= 4; i++) {
            const optionText = quiz["option" + i];
            const isSelected = selectedAnswers[currentQuizIndex] === i;

            const $btn = $(`
                <button class="btn white" type="button" data-choice="${i}">
                    <span class="choice-no">${i}.</span>
                    <span class="choice-text">${optionText}</span>
                </button>
            `);

            if (isSelected) {
                $btn.addClass("selected");
            }

            $btn.on("click", function() {
                if ($(this).hasClass('hint-removed')) {
                    return;
                }

                selectedAnswers[currentQuizIndex] = i;
                $quizOptions.find("button").removeClass("selected");
                $(this).addClass("selected");
            });

            $quizOptions.append($btn);
        }

        const isHintUsed = usedHints[currentQuizIndex];
        if (isHintUsed && quiz.hintRemovedOption) {
            disableOption(quiz.hintRemovedOption);
        }
    }

    function updateHintButton() {
        const userHintCount = parseInt($hintCount.text());
        const isHintUsed = usedHints[currentQuizIndex];

        if (userHintCount <= 0 || isHintUsed) {
            $hintBtn.prop('disabled', true).addClass('disabled');
        } else {
            $hintBtn.prop('disabled', false).removeClass('disabled');
        }
    }

    function updateNavigationButtons() {
        $prevBtn.prop("disabled", currentQuizIndex === 0);
        $nextBtn.text(currentQuizIndex === quizzes.length - 1 ? "제출" : "다음");
    }

    function updateProgressBar() {
        const progressPercent = ((currentQuizIndex + 1) / quizzes.length) * 100;
        $progressBar.css('width', progressPercent + '%');
    }

    $hintBtn.off("click").on("click", function () {
        const userHintCount = parseInt($hintCount.text());

        if (userHintCount <= 0) {
            alert("사용 가능한 힌트가 없습니다.");
            return;
        }

        if (usedHints[currentQuizIndex]) {
            alert("이미 힌트를 사용한 문제입니다.");
            return;
        }

        if (!confirm("힌트를 사용하시겠습니까? (1개 차감)")) {
            return;
        }

        const currentQuiz = quizzes[currentQuizIndex];
        const removedOption = getRandomWrongOption(currentQuiz);

        disableOption(removedOption);
        usedHints[currentQuizIndex] = true;
        currentQuiz.hintRemovedOption = removedOption;
        updateHintButton();

        useHintAjax(currentQuizIndex, removedOption)
        .then(response => {
            if (response.success && response.data) {
                const remainingHints = response.data.remainingHints || response.data;
                $hintCount.text(remainingHints).addClass("action");
            } else {
                const newHintCount = parseInt($hintCount.text()) - 1;
                $hintCount.text(newHintCount).addClass("action");
            }

            setTimeout(() => $hintCount.removeClass("action"), 200);
        })
        .catch(() => {
            alert("힌트 사용에 실패했습니다. 다시 시도해주세요.");
        });
    });

    $prevBtn.off("click").on("click", function () {
        if (currentQuizIndex > 0) {
            currentQuizIndex--;
            renderLevelQuiz();
        }
    });

    $nextBtn.off("click").on("click", function () {
        if (currentQuizIndex < quizzes.length - 1) {
            currentQuizIndex++;
            renderLevelQuiz();
        } else {
            if (confirm("퀴즈를 제출하시겠습니까?")) {
                submitResults('level', { totalTimeSec: "0.0" });
            }
        }
    });
}

async function useHintAjax(quizIndex, removedOption) {
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
        return response;
    } catch (e) {
        console.error("힌트 사용 반영 실패", e);
        throw e;
    }
}

function initSpeedQuiz() {
    console.log("스피드 퀴즈 초기화");

    const TIME_LIMIT_SEC = 10;
    const $timerContainer = $(".progress_container");
    const $quizTimer = $("#quiz-timer");
    const $timerProgressBar = $("#timer-progress-bar");
    const $quizCallout = $("#quiz-callout");

    let timerInterval = null;
    let timeLeft = TIME_LIMIT_SEC;
    let quizStartTime = Date.now();

    renderSpeedQuiz();

    function renderSpeedQuiz() {
        const quiz = quizzes[currentQuizIndex];

        renderQuizQuestion();
        renderSpeedQuizOptions();

        $quizCallout.text(quiz.questionType);

        startTimer();
    }

    function renderSpeedQuizOptions() {
        const quiz = quizzes[currentQuizIndex];
        $quizOptions.empty();

        let options = "";
        for (let i = 0; i < quiz.options.length; i++) {
            options += `
                <button type="button" class="btn white option_btns" data-choice="${i + 1}">
                    <span class="choice-no">${i + 1}.</span>
                    <span class="choice-text">${quiz.options[i]}</span>
                </button>
            `;
        }
        $quizOptions.append(options);
    }

    $(document).on("click", ".option_btns", function () {
        const choiceNumber = Number($(this).data("choice"));
        selectedAnswers[currentQuizIndex] = choiceNumber;
        console.log(`문제 ${currentQuizIndex + 1}: 보기 ${choiceNumber} 선택`);
        handleSubmit();
    });

    function startTimer() {
        clearInterval(timerInterval);
        timeLeft = TIME_LIMIT_SEC;
        const totalTime = timeLeft;

        $timerContainer.removeClass("warning");
        $quizTimer.text(totalTime + " 초");

        $timerProgressBar.stop(true, true)
        .css({width: "100%"})
        .animate({width: 0}, totalTime * 1000, "linear");

        timerInterval = setInterval(function () {
            timeLeft--;

            if (timeLeft <= 3) {
                $timerContainer.addClass("warning");
            }

            $quizTimer.text((timeLeft < 10 ? "0" + timeLeft : timeLeft) + " 초");

            if (timeLeft <= 0) {
                clearInterval(timerInterval);
                handleSubmit();
            }
        }, 1000);
    }

    function handleSubmit() {
        clearInterval(timerInterval);
        goToNextQuestion();
    }

    function goToNextQuestion() {
        currentQuizIndex++;

        if (currentQuizIndex < quizzes.length) {
            renderSpeedQuiz();
        } else {
            console.log("퀴즈 완료! 결과 폼 제출");

            const totalTimeSec = Math.floor((Date.now() - quizStartTime) / 1000);
            submitResults('speed', { totalTimeSec: totalTimeSec });
        }
    }
}

function initQuizExitModal() {
    const $quizEndBtn = $(".quiz_end_btn");
    const $modalQuizExit = $("#modal-quiz-exit");

    $quizEndBtn.click(function () {
        if ($modalQuizExit.length) {
            $modalQuizExit.show();

            setTimeout(() => {
                $modalQuizExit.addClass("open");
            }, 10);
        }
    });
}