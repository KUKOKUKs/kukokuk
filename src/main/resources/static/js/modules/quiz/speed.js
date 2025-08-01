import {
  getSpeedQuizList,
  insertSpeedQuizResult
} from "/js/modules/quiz/speed-api.js";

$(document).ready(async function () {
  let quizzes = [];
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

  try {
    quizzes = await getSpeedQuizList();
    if (!Array.isArray(quizzes) || quizzes.length === 0) {
      alert("퀴즈를 불러오지 못했습니다.");
      return;
    }

    selectedAnswers = new Array(quizzes.length).fill(null);
    $quizTotal.text(quizzes.length);
    currentQuizIndex = 0;
    renderCurrentQuiz();
  } catch (e) {
    console.error("퀴즈 불러오기 실패:", e);
  }

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

  async function goToNextQuestion() {
    currentQuizIndex++;
    $quizTimer.removeClass("low-time");

    if (currentQuizIndex < quizzes.length) {
      renderCurrentQuiz();
    } else {
      console.log("퀴즈 완료! 결과 저장 요청 시작");

      const totalTimeSec = Math.floor((Date.now() - quizStartTime) / 1000);

      const results = quizzes.map((quiz, idx) => ({
        quizNo: quiz.quizNo,
        selectedChoice: selectedAnswers[idx],
        isBookmarked: "N"
      }));

      const payload = {
        userNo: 1,
        totalTimeSec: totalTimeSec,
        results: results
      };

      try {
        const sessionNo = await insertSpeedQuizResult(payload);
        console.log("결과 저장 성공, sessionNo:", sessionNo);
        window.location.href = `/quiz/speed-result?sessionNo=${sessionNo}&userNo=1`;
      } catch (error) {
        console.error("결과 저장 실패:", error);
        alert("결과 저장 중 오류가 발생했습니다.");
      }
    }
  }
});
