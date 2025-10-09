$(document).ready(function () {
    let currentQuizIndex = 0;
    let selectedAnswers = [];
    let timerInterval = null;
    let timeLeft = 10;
    let quizStartTime = Date.now();

    const $timerContainer = $(".progress_container"); // 타이머 컨테이너 요소 - 수정 사항으로 추가됨
    // const $quizTotal = $("#quiz-total"); // 컨트롤러에서 내려준 리스트의 size로 타임리프 th:text로 즉시 적용으로 선택자 불필요
    const $quizCurrent = $("#quiz-current");
    const $quizQuestion = $("#quiz-question");
    const $quizCallout = $("#quiz-callout");
    const $quizOptions = $("#quiz-options");
    const $quizTimer = $("#quiz-timer");
    const $timerBar = $("#timer-progress-bar");

    // 컨트롤러에서 내려줄 때 조건에 따라 처리
    // 예기치 못한 오류로 인해 아래와 같은 예외 처리 로직이 있다면 결과 처리까지도 필요함
    // 현재 데이터를 못 불러온 상황이라면 오류가난 엉망인 페이지에서
    // alert만 띄우고 말거면 경고창 없이 루트페이지를 가는게 나아보임
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
        // 몇 번째 문제인지는 타이머에 나오므로 단어인지 뜻인지 표시가 더 나아보임
        // 간단한 css는 클라스로 지정 자바스크립트 내 로직은 가볍게 가는게 좋음(선택사항/크게차이는 없음)
        // 수정사항 적용 css는 html tag class로 지정
        $quizCallout.text(quiz.questionType);
        $quizQuestion.text(quiz.question);
        $quizOptions.empty();

        /*
            스타일 조정으로 tag, class 수정
            append시 매번 아래와같이 해도 괜찮으나 한번에 처리하는 깔끔한 로직 구성이 가독성면에서 좋아보임
            옵션들을 한번에 받아와 처리하는게 효율적임
            개발자가 직접 지정한 값은 유동정이지 않으며 동적으로 시스템 반영 안됨
            시작점은 지정이 가능하나 조건(i <= 4)은 올바르지 않음
            사용처가 없는 data-choice="${i}" 이러한 불필요한 속성 추가는 하지않도록 주의(가독성/유지보수 등에서 오해,불편을 발생시키고 개념,의도 전달 불확실)
            $btn.on("click", <- 해당 이벤트로 직접 처리로 인하여 data-choice="${i}"는 없어도 되는 구성임 왜 넣었는지 의문이 듦
            dataset 속성을 추가했다면 이벤트 핸들러를 따로 구성해도 깔끔하고 괜찮았을것 같음
        */
        // for (let i = 1; i <= 4; i++) {
        //     const optionText = quiz["option" + i];
        //     const $btn = $(`<button class="btn white" data-choice="${i}">
        //         <span class="choice-no">${i}.</span> ${optionText}
        //     </button>`);
        //
        //     $btn.on("click", function () {
        //         selectedAnswers[currentQuizIndex] = i;
        //         console.log(`문제 ${currentQuizIndex + 1}: 보기 ${i} 선택`);
        //         handleSubmit();
        //     });
        //
        //     $quizOptions.append($btn);
        // }
        // 위 수정 사항 적용 로직
        let options = "";
        for (let i = 1; i <= quiz.options.length; i++) {
            options+= `
                <button type="button" class="component_info option_btns" data-choice="${i}">
                    <div class="list_option">
                        <span class="marker">${i}.</span>
                        <p class="option">${quiz.options[i - 1]}</p>
                    </div>
                </button>
            `;
        }
        $quizOptions.append(options);

        startTimer();
    }
    
    // 위 버튼 이벤트 핸들러 추가(확인요망)
    $(document).on("click", ".option_btns", function () {
        const choiceNumber = Number($(this).data("choice"));
        selectedAnswers[currentQuizIndex] = choiceNumber;
        console.log(`문제 ${currentQuizIndex + 1}: 보기 ${choiceNumber} 선택`);
        handleSubmit();
    });

    function startTimer() {
        clearInterval(timerInterval);
        timeLeft = 10;
        const totalTime = timeLeft;

        // 초기화는 상위 부모 요소에 하나로 조정하고 디테일 조정만 초기화
        // 연속 호출 되는 곳에서는 정확히 원하는 시점 제어가 까다로워 transition 대신 제이쿼리로 해결(선택사항)
        // 1. 상위 부모 요소에 클라스 제거로 기본 초기화
        // 2. 전체적인 기본 로직 개념은 타이머의 초기화가 되므로 타이머시간 설정한 시간값으로 적용
        // $timerBar.removeClass("after_quiz").css({
        //     transition: "none",
        //     width: "100%"
        // });
        // 타이머 초기화
        $timerContainer.removeClass("warning"); // 1번 적용
        $quizTimer.text(totalTime + " 초"); // 2번 적용

        // reflow
        // void $timerBar[0].offsetWidth; // 이 부분은 왜 필요한건지 어떤 기능을 하는건지? // 제이쿼리 사용 시 필요없음

        // css로 적용하지 않고 이런 경우는 jQuery의 animate사용(여러곳에서 반복되는 animate가 아닌 이 요소에 대해서 단순한 animate로 가볍게 사용)
        // width 줄이기
        // $timerBar.css({
        //     transition: `width ${totalTime}s linear`,
        //     width: "0%"
        // });
        $timerBar.stop(true, true)  // 진행 중인 애니메이션을 즉시 중단 + 큐 제거
            .css({width: "100%"}) // 초기화
            .animate({width: 0}, totalTime * 1000, "linear"); // 새 애니메이션 시작

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
