$(document).ready(function () {
    let currentQuizIndex = 0;
    let selectedAnswers = [];
    let timerInterval = null;
    let timeLeft = 10;
    let quizStartTime = Date.now();

    const $timerContainer = $(".timer_container"); // 타이머 컨테이너 요소 - 수정 사항으로 추가됨
    // const $quizTotal = $("#quiz-total"); // 컨트롤러에서 내려준 리스트의 size로 타임리프 th:text로 즉시 적용으로 선택자 불필요
    const $quizCurrent = $("#quiz-current");
    const $quizQuestion = $("#quiz-question");
    const $quizOptions = $("#quiz-options");
    const $quizTimer = $("#quiz-timer");
    const $timerBar = $("#timer-progress-bar");

    // 컨트롤러에서 내려줄 때 조건에 따라 처리
    // if (!Array.isArray(quizzes) || quizzes.length === 0) {
    //     alert("퀴즈를 불러오지 못했습니다.");
    //     return;
    // }

    // selectedAnswers array 처리는 자바스크립트에서는 불필요
    // (자바스크립트는 자동으로 가지는 리스트만큼 배열 크기가 적용됨)
    // selectedAnswers = new Array(quizzes.length).fill(null);
    // $quizTotal.text(quizzes.length); // 컨트롤러에서 내려준 리스트의 size로 타임리프 th:text로 즉시 적용
    // currentQuizIndex = 0; // let currentQuizIndex = 0; 위에 이미 선언되어 있음(똑같은 값 중복 대입)
    
    renderCurrentQuiz();

    function renderCurrentQuiz() {
        const quiz = quizzes[currentQuizIndex];
        $quizCurrent.text(currentQuizIndex + 1);
        $quizQuestion.text(`Q${currentQuizIndex + 1}. ${quiz.question}`).css(
            "text-align", "center");
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
        const totalTime = timeLeft;

        // 초기화는 상위 부모 요소에 하나로 조정하고 디테일 조정만 초기화
        // 1. 상위 부모 요소에 클라스 제거로 기본 초기화
        // 2. 타이머 진행바 조정은 css로 즉시 적용되도록 기본 css에서 transition width 제거함
        // 3. 전체적인 기본 로직 개념은 타이머의 초기화가 되므로 타이머시간 설정한 시간값으로 적용
        // $timerBar.removeClass("after_quiz").css({
        //     transition: "none",
        //     width: "100%"
        // });
        // 타이머 초기화
        $timerContainer.removeClass("warning"); // 1번 적용
        $timerBar.css({width: "100%"}); // 2번 적용
        $quizTimer.text(timeLeft + " 초"); // 3번 적용

        // reflow
        // void $timerBar[0].offsetWidth; // 이 부분은 왜 필요한건지 어떤 기능을 하는건지?

        // css로 적용하지 않고 이런 경우는 jQuery의 animate사용(여러곳에서 반복되는 animate가 아닌 이 요소에 대해서 단순한 animate로 가볍게 사용)
        // width 줄이기
        // $timerBar.css({
        //     transition: `width ${totalTime}s linear`,
        //     width: "0%"
        // });
        $timerBar.animate({width: 0}, (totalTime * 1000), "linear");

        // 숫자 & 색상 갱신
        timerInterval = setInterval(function () {
            timeLeft--;

            // 3초, 5초별로 구분지어 색상 변경이 필요할지 의문임 3초만 적용해도 괜찮아 보임
            // 상위 요소 하나만 변경해도 적용되도록 간편화한 css 적용
            // 아래 주석처리된 로직은 삭제해도 됨
            // 3초 이하일 때 색상 변경
            // if (timeLeft <= 3) {
            //   $timerBar.addClass("after_quiz");
            // }
            // if (timeLeft <= 5) {
            //   $quizTimer.addClass("low-time");
            // }
            // 위 수정 사항 적용 로직
            if (timeLeft <= 3) {
                $timerContainer.addClass("warning");
            }

            // 10초인데 분단위의 표시가 무의미해 보임 아래는 수정 사항 적용
            // 초기화할때도 필요 아래만 적용 시 사용자는 9초부터 보임
            $quizTimer.text((timeLeft < 10 ? "0" + timeLeft : timeLeft) + " 초");

            // 시간 종료
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
        // $quizTimer.removeClass("low-time"); // 불필요

        if (currentQuizIndex < quizzes.length) {
            renderCurrentQuiz();
        } else {
            console.log("퀴즈 완료! 결과 폼 제출");

            const totalTimeSec = Math.floor(
                (Date.now() - quizStartTime) / 1000);
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

            // $("body").append($form); // 이미 html로 요소가 있는 상태인데 추가 폼 생성되므로 제거
            $form.submit();
        }
    }
});
