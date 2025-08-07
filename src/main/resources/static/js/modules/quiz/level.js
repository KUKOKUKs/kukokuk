$(document).ready(function () {
    let currentQuizIndex = 0;
    let selectedAnswers = [];

    const $quizTotal = $("#quiz-total");
    const $quizCurrent = $("#quiz-current");
    const $quizQuestion = $("#quiz-question");
    const $quizOptions = $("#quiz-options");

    if (!Array.isArray(quizzes) || quizzes.length === 0) {
        alert("퀴즈를 불러오지 못했습니다.");
        return;
    }

    selectedAnswers = new Array(quizzes.length).fill(null);
    $quizTotal.text(quizzes.length);
    currentQuizIndex = 0;
    renderCurrentQuiz();

    function renderCurrentQuiz() {
        const quiz = quizzes[currentQuizIndex];
        $quizCurrent.text(currentQuizIndex + 1);
        $quizQuestion.text(`Q${currentQuizIndex + 1}. ${quiz.question}`).css("text-align", "center");
        $quizOptions.empty();

        for (let i = 1; i <= 4; i++) {
            const optionText = quiz["option" + i];
            const $btn = $(`<button class="btn white" data-choice="${i}">
                        <span class="choice-no">${i}.</span> ${optionText}
                    </button>`);

            $btn.on("click", function () {
                selectedAnswers[currentQuizIndex] = i;
                goToNextQuestion();
            });

            $quizOptions.append($btn);
        }
    }

    function goToNextQuestion() {
        currentQuizIndex++;

        if (currentQuizIndex < quizzes.length) {
            renderCurrentQuiz();
        } else {
            // 폼 전송
            const $form = $("#quiz-result-form");

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

            $("body").append($form);
            $form.submit();
        }
    }
});
