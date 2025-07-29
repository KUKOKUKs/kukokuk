let quizzes = [];
let currentQuizIndex = 0;
let selectedAnswers = [];
let timerInterval = null;
let timeLeft = 10;

document.addEventListener("DOMContentLoaded", () => {
  fetch("/api/quiz/speed")
  .then(response => response.json())
  .then(data => {
    if (!Array.isArray(data) || data.length === 0) {
      alert("퀴즈를 불러오지 못했습니다.");
      return;
    }

    quizzes = data;
    document.getElementById("quiz-total").textContent = quizzes.length;
    startQuiz();
  })
  .catch(error => {
    console.error("퀴즈 불러오기 실패", error);
  });
});

function startQuiz() {
  if (quizzes.length === 0) return;
  currentQuizIndex = 0;
  selectedAnswers = new Array(quizzes.length).fill(null);
  renderCurrentQuiz();
}

function renderCurrentQuiz() {
  const quiz = quizzes[currentQuizIndex];
  document.getElementById("quiz-current").textContent = currentQuizIndex + 1;
  const questionElement = document.getElementById("quiz-question");
  questionElement.textContent = `Q${currentQuizIndex + 1}. ${quiz.question}`;
  questionElement.style.textAlign = "center"; // ← 문제 중앙 정렬

  const optionsDiv = document.getElementById("quiz-options");
  optionsDiv.innerHTML = "";

  for (let i = 1; i <= 4; i++) {
    const optionText = quiz["option" + i];
    const btn = document.createElement("button");
    btn.innerHTML = `<span class="choice-no">${i}.</span> ${optionText}`; // ← 문항 앞 번호 표시
    btn.classList.add("btn", "white");
    btn.dataset.choice = i;

    btn.addEventListener("click", () => {
      selectedAnswers[currentQuizIndex] = i;
      console.log(`문제 ${currentQuizIndex + 1}: 선택한 보기 ${i} (${optionText})`);
      handleSubmit();
    });

    optionsDiv.appendChild(btn);
  }

  startTimer();
}


function startTimer() {
  clearInterval(timerInterval);
  timeLeft = 10;

  const timerElement = document.getElementById("quiz-timer");
  timerElement.classList.remove("low-time");
  timerElement.textContent = "00:10";

  timerInterval = setInterval(() => {
    timeLeft--;

    if (timeLeft <= 5) {
      timerElement.classList.add("low-time");
    }

    timerElement.textContent = "00:" + (timeLeft < 10 ? "0" + timeLeft : timeLeft);

    if (timeLeft <= 0) {
      clearInterval(timerInterval);
      console.log(`문제 ${currentQuizIndex + 1}: 시간 초과로 정답 미선택`);
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
  const timerElement = document.getElementById("quiz-timer");
  timerElement.classList.remove("low-time");

  if (currentQuizIndex < quizzes.length) {
    renderCurrentQuiz();
  } else {
    console.log("모든 퀴즈 완료. 결과 페이지로 이동합니다.");
    window.location.href = "/quiz/result";
  }
}
