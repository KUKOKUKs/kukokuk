$(document).ready(function () {
    let currentQuizIndex = 0;
    let selectedAnswers = [];
    const $quizTotal = $("#quiz-total");
    const $quizCurrent = $("#quiz-current");
    const $quizQuestion = $("#quiz-question");
    const $quizOptions = $("#quiz-options");
    const $modal = $("#submit-modal");

    // **모달 무조건 hide (초기화)**
    $modal.hide();

    console.log("quizzes length =", quizzes.length); // 데이터 확인용

    if (!Array.isArray(quizzes) || quizzes.length === 0) {
        alert("퀴즈를 불러오지 못했습니다.");
        return;
    }

    selectedAnswers = new Array(quizzes.length).fill(null);
    $quizTotal.text(quizzes.length);
    $quizCurrent.text(1);
    renderCurrentQuiz();

    function renderCurrentQuiz() {
        const quiz = quizzes[currentQuizIndex];
        $quizCurrent.text(currentQuizIndex + 1);
        $quizQuestion.text(`Q${currentQuizIndex + 1}. ${quiz.question}`).css("text-align", "center");
        $quizOptions.empty();

        for (let i = 1; i <= 4; i++) {
            const optionText = quiz["option" + i];
            const isSelected = selectedAnswers[currentQuizIndex] === i;
            const $btn = $(`<button class="btn white" type="button" data-choice="${i}">
                <span class="choice-no">${i}.</span> ${optionText}
            </button>`);
            if (isSelected) {
                $btn.addClass("selected");
            }
            $btn.on("click", function () {
                selectedAnswers[currentQuizIndex] = i;
                $quizOptions.find("button").removeClass("selected");
                $(this).addClass("selected");
            });
            $quizOptions.append($btn);
        }

        // 이전/다음 버튼 상태 및 텍스트 제어
        $("#prev-btn").prop("disabled", currentQuizIndex === 0);
        $("#next-btn").text(currentQuizIndex === quizzes.length - 1 ? "제출" : "다음");

        // **문제 렌더링마다도 모달 hide**
        $modal.hide();
    }

    $("#prev-btn").off("click").on("click", function() {
        if (currentQuizIndex > 0) {
            currentQuizIndex--;
            renderCurrentQuiz();
        }
    });

    $("#next-btn").off("click").on("click", function() {
        if (currentQuizIndex < quizzes.length - 1) {
            currentQuizIndex++;
            renderCurrentQuiz();
        } else {
            // 마지막 문제에서만 show
            $modal.show();
        }
    });

    $("#hint-btn").off("click").on("click", function () {
        alert("힌트 기능은 추후 지원 예정입니다.");
    });

    $("#submit-confirm-btn").off("click").on("click", function() {
        submitResults();
    });
    $("#submit-cancel-btn").off("click").on("click", function() {
        $modal.hide();
    });

    function submitResults() {
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
        });
        $form.submit();
    }
});
