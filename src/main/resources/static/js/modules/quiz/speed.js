$(document).ready(function () {
  let currentQuizIndex = 0;
  let selectedAnswers = [];
  let timerInterval = null;
  let timeLeft = 10;
  let quizStartTime = Date.now();

  const $quizTotal = $("#quiz-total");
  const $quizCurrent = $("#quiz-current");
  const $quizQuestion = $("#quiz-question");
  const $quizOptions = $("#quiz-options");
  const $quizTimer = $("#quiz-timer");

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
        console.log(`문제 ${currentQuizIndex + 1}: 보기 ${i} 선택`);
        handleSubmit();
      });

      $quizOptions.append($btn);
    }

    startTimer();
  }

  function startTimer() {
    clearInterval(timerInterval);
    timeLeft = 10;
    $quizTimer.removeClass("low-time").text("00:10");

    timerInterval = setInterval(function () {
      timeLeft--;
      if (timeLeft <= 5) $quizTimer.addClass("low-time");
      $quizTimer.text("00:" + (timeLeft < 10 ? "0" + timeLeft : timeLeft));

      if (timeLeft <= 0) {
        clearInterval(timerInterval);
        console.log(`문제 ${currentQuizIndex + 1}: 시간 초과`);
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
    $quizTimer.removeClass("low-time");

    if (currentQuizIndex < quizzes.length) {
      renderCurrentQuiz();
    } else {
      console.log("퀴즈 완료! 결과 폼 제출");

      const totalTimeSec = Math.floor((Date.now() - quizStartTime) / 1000);
      const $form = $("#quiz-result-form");

      $form.append($("<input>", {
        type: "hidden",
        name: "totalTimeSec",
        value: totalTimeSec
      }));

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
