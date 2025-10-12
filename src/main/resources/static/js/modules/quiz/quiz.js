/**
 * 통합 퀴즈 JavaScript
 * Speed 퀴즈와 Level 퀴즈를 하나의 파일로 통합하여 관리
 * quizMode 변수로 모드를 구분하여 각각의 로직을 처리
 */
$(document).ready(function () {
    // quizMode 변수 검증
    if (typeof quizMode === 'undefined') {
        console.error("quizMode 변수가 정의되지 않았습니다. HTML에서 quizMode를 선언해주세요.");
        return;
    }

    // quizzes 데이터 검증
    if (typeof quizzes === 'undefined' || !Array.isArray(quizzes) || quizzes.length === 0) {
        console.error("퀴즈 데이터를 불러오지 못했습니다.");
        return;
    }

    // 전역 변수 선언
    let currentQuizIndex = 0;                    // 현재 퀴즈 인덱스
    let selectedAnswers = [];                    // 사용자가 선택한 답안 배열
    let usedHints = [];                          // 힌트 사용 여부 배열 (Level 전용)
    let timerInterval = null;                    // 타이머 인터벌 객체 (Speed 전용)
    let timeLeft = 10;                           // 남은 시간 (Speed 전용)
    let quizStartTime = Date.now();              // 퀴즈 시작 시간

    // 모드 구분 플래그
    const isSpeedMode = quizMode === "speed";
    const isLevelMode = quizMode === "level";

    // DOM 요소 캐싱
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
    const $prevBtn = $("#prev-btn");
    const $nextBtn = $("#next-btn");

    // Level 모드일 경우 힌트 사용 배열 초기화
    if (isLevelMode) {
        usedHints = new Array(quizzes.length).fill(false);
    }

    // 첫 번째 퀴즈 렌더링
    renderCurrentQuiz();

    /**
     * 현재 퀴즈를 화면에 렌더링
     * Speed/Level 모드에 따라 다른 UI 요소를 표시
     */
    function renderCurrentQuiz() {
        const quiz = quizzes[currentQuizIndex];
        $quizCurrent.text(currentQuizIndex + 1);

        // 문제 유형 표시 (Speed 모드에서만)
        if ($quizCallout.length) {
            $quizCallout.text(quiz.questionType);
        }

        // 문제 텍스트 표시
        $quizQuestion.text(quiz.question);
        $quizOptions.empty();

        // 보기 버튼 생성
        let options = "";
        for (let i = 1; i <= quiz.options.length; i++) {
            const isDisabled = isLevelMode && usedHints[currentQuizIndex] && quiz.hintRemovedOption === i;
            const isSelected = selectedAnswers[currentQuizIndex] === i;
            const disabledClass = isDisabled ? "hint-removed" : "";
            const selectedClass = isSelected ? "selected" : "";

            options += `
                <button type="button" class="component_info option_btns ${disabledClass} ${selectedClass}" 
                        data-choice="${i}" ${isDisabled ? 'disabled' : ''}>
                    <div class="list_option">
                        <span class="marker">${i}.</span>
                        <p class="option">${quiz.options[i - 1]}</p>
                    </div>
                </button>
            `;
        }
        $quizOptions.append(options);

        // 모드별 추가 UI 업데이트
        if (isLevelMode) {
            updateHintButton();
            updateNavigationButtons();
            renderProgressBar();
        } else if (isSpeedMode) {
            startTimer();
        }
    }

    /**
     * 보기 선택 이벤트 핸들러
     * Speed 모드: 선택 즉시 다음 문제로 이동
     * Level 모드: 선택만 하고 대기
     */
    $(document).on("click", ".option_btns:not(.hint-removed)", function () {
        const choiceNumber = Number($(this).data("choice"));

        // 보기 선택 UI 업데이트
        $quizOptions.find(".option_btns").removeClass("selected");
        $(this).addClass("selected");

        selectedAnswers[currentQuizIndex] = choiceNumber;
        console.log(`문제 ${currentQuizIndex + 1}: 보기 ${choiceNumber} 선택`);

        // Speed 모드에서는 선택 즉시 제출
        if (isSpeedMode) {
            handleSubmit();
        }
    });

    /**
     * 타이머 시작 (Speed 모드 전용)
     * 10초 카운트다운 및 진행 바 애니메이션
     */
    function startTimer() {
        if (!isSpeedMode) return;

        clearInterval(timerInterval);
        timeLeft = 10;
        const totalTime = timeLeft;

        // 타이머 초기화
        $timerContainer.removeClass("warning");
        $quizTimer.text(totalTime + " 초");

        // 진행 바 애니메이션
        $timerBar.stop(true, true)
        .css({width: "100%"})
        .animate({width: 0}, totalTime * 1000, "linear");

        // 1초마다 타이머 업데이트
        timerInterval = setInterval(function () {
            timeLeft--;

            // 3초 이하일 때 경고 표시
            if (timeLeft <= 3) {
                $timerContainer.addClass("warning");
            }

            $quizTimer.text((timeLeft < 10 ? "0" + timeLeft : timeLeft) + " 초");

            // 시간 종료 시 자동 제출
            if (timeLeft <= 0) {
                clearInterval(timerInterval);
                handleSubmit();
            }
        }, 1000);
    }

    /**
     * 답안 제출 처리 (Speed 모드 전용)
     */
    function handleSubmit() {
        if (!isSpeedMode) return;

        clearInterval(timerInterval);
        goToNextQuestion();
    }

    /**
     * 다음 문제로 이동 (Speed 모드 전용)
     * 마지막 문제일 경우 결과 제출
     */
    function goToNextQuestion() {
        if (!isSpeedMode) return;

        currentQuizIndex++;

        if (currentQuizIndex < quizzes.length) {
            renderCurrentQuiz();
        } else {
            console.log("퀴즈 완료! 결과 폼 제출");
            submitResults();
        }
    }

    /**
     * 힌트 버튼 상태 업데이트 (Level 모드 전용)
     * 힌트 개수가 0개이거나 이미 사용한 문제면 비활성화
     */
    function updateHintButton() {
        if (!isLevelMode || !$hintBtn.length) return;

        const userHintCount = parseInt($hintCount.text());

        if (userHintCount <= 0 || usedHints[currentQuizIndex]) {
            $hintBtn.addClass("disabled").prop("disabled", true);
        } else {
            $hintBtn.removeClass("disabled").prop("disabled", false);
        }
    }

    /**
     * 힌트 버튼 클릭 이벤트 (Level 모드 전용)
     * 정답이 아닌 보기 중 하나를 랜덤하게 비활성화
     */
    $hintBtn.off("click").on("click", function () {
        if (!isLevelMode) return;

        const userHintCount = parseInt($hintCount.text());

        // 힌트 개수 체크
        if (userHintCount <= 0) {
            alert("사용 가능한 힌트가 없습니다.");
            return;
        }

        // 이미 힌트를 사용한 문제인지 체크
        if (usedHints[currentQuizIndex]) {
            alert("이미 힌트를 사용한 문제입니다.");
            return;
        }

        // 사용 확인
        if (!confirm("힌트를 사용하시겠습니까? (1개 차감)")) {
            return;
        }

        const currentQuiz = quizzes[currentQuizIndex];
        const removedOption = getRandomWrongOption(currentQuiz);

        // 보기 비활성화 및 상태 업데이트
        disableOption(removedOption);
        usedHints[currentQuizIndex] = true;
        currentQuiz.hintRemovedOption = removedOption;

        updateHintButton();
        useHintApi(currentQuizIndex, removedOption);
    });

    /**
     * 정답이 아닌 보기 중 하나를 랜덤하게 선택
     * @param {Object} quiz - 퀴즈 객체
     * @return {number} 제거할 보기 번호 (1~4)
     */
    function getRandomWrongOption(quiz) {
        const correctAnswer = quiz.successAnswer;
        const wrongOptions = [];

        // 정답이 아닌 보기 수집
        for (let i = 1; i <= quiz.options.length; i++) {
            if (i !== correctAnswer) {
                wrongOptions.push(i);
            }
        }

        // 랜덤 선택
        const randomIndex = Math.floor(Math.random() * wrongOptions.length);
        return wrongOptions[randomIndex];
    }

    /**
     * 지정된 보기를 비활성화 처리
     * @param {number} optionNumber - 비활성화할 보기 번호 (1~4)
     */
    function disableOption(optionNumber) {
        const $optionBtn = $quizOptions.find(`button[data-choice="${optionNumber}"]`);

        // 버튼 비활성화 및 스타일 적용
        $optionBtn
        .addClass("hint-removed")
        .prop("disabled", true)
        .off("click");

        // 시각적 효과 (취소선, 흐리게)
        $optionBtn.find(".option").css({
            "text-decoration": "line-through",
            "opacity": "0.5",
            "color": "#999"
        });

        // 선택되어 있었다면 선택 해제
        if ($optionBtn.hasClass("selected")) {
            $optionBtn.removeClass("selected");
            selectedAnswers[currentQuizIndex] = null;
        }
    }

    /**
     * 힌트 사용 API 호출
     * 서버에 힌트 사용을 기록하고 남은 힌트 개수를 업데이트
     * @param {number} quizIndex - 퀴즈 인덱스
     * @param {number} removedOption - 제거된 보기 번호
     */
    async function useHintApi(quizIndex, removedOption) {
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

            // 서버에서 반환된 남은 힌트 개수로 UI 업데이트
            if (response.success && response.data !== undefined) {
                const remainingHints = response.data;
                $hintCount.text(remainingHints).addClass("action");
            } else {
                // 서버 응답이 없을 경우 클라이언트에서 차감
                const newHintCount = parseInt($hintCount.text()) - 1;
                $hintCount.text(newHintCount).addClass("action");
            }

            // 액션 효과 제거
            setTimeout(() => $hintCount.removeClass("action"), 200);

        } catch (e) {
            console.error("힌트 사용 반영 실패", e);
            alert("힌트 사용에 실패했습니다. 다시 시도해주세요.");
        }
    }

    /**
     * 진행률 바 업데이트 (Level 모드 전용)
     * 현재 문제 번호에 따라 진행률 표시
     */
    function renderProgressBar() {
        if (!isLevelMode || !$progressBar.length) return;

        const progressPercent = ((currentQuizIndex + 1) / quizzes.length) * 100;
        $progressBar.css("width", progressPercent + "%");
    }

    /**
     * 이전/다음 버튼 상태 업데이트 (Level 모드 전용)
     * 첫 문제에서는 이전 버튼 비활성화
     * 마지막 문제에서는 다음 버튼이 제출 버튼으로 변경
     */
    function updateNavigationButtons() {
        if (!isLevelMode) return;

        if ($prevBtn.length) {
            $prevBtn.prop("disabled", currentQuizIndex === 0);
        }

        if ($nextBtn.length) {
            $nextBtn.text(currentQuizIndex === quizzes.length - 1 ? "제출" : "다음");
        }
    }

    /**
     * 이전 버튼 클릭 이벤트 (Level 모드 전용)
     */
    $prevBtn.off("click").on("click", function () {
        if (!isLevelMode) return;

        if (currentQuizIndex > 0) {
            currentQuizIndex--;
            renderCurrentQuiz();
        }
    });

    /**
     * 다음 버튼 클릭 이벤트 (Level 모드 전용)
     * 마지막 문제일 경우 제출 확인 후 결과 전송
     */
    $nextBtn.off("click").on("click", function () {
        if (!isLevelMode) return;

        if (currentQuizIndex < quizzes.length - 1) {
            currentQuizIndex++;
            renderCurrentQuiz();
        } else {
            if (confirm("퀴즈를 제출하시겠습니까?")) {
                submitResults();
            }
        }
    });

    /**
     * 퀴즈 결과 제출
     * 숨겨진 폼에 결과 데이터를 추가하고 서버로 전송
     */
    function submitResults() {
        console.log("퀴즈 완료! 결과 폼 제출");

        // 총 소요 시간 계산 (Speed 모드에서만)
        const totalTimeSec = isSpeedMode
            ? Math.floor((Date.now() - quizStartTime) / 1000)
            : 0;

        const $form = $("#quiz-result-form");
        $form.empty();

        // 공통 데이터
        $form.append($("<input>", {
            type: "hidden",
            name: "totalTimeSec",
            value: totalTimeSec
        }));

        $form.append($("<input>", {
            type: "hidden",
            name: "quizMode",
            value: quizMode
        }));

        // CSRF 토큰 추가
        const csrfToken = $("meta[name='_csrf']").attr("content");
        if (csrfToken) {
            $form.append($("<input>", {
                type: "hidden",
                name: "_csrf",
                value: csrfToken
            }));
        }

        // 각 문제의 결과 데이터
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

            // Level 모드 전용: 힌트 사용 정보
            if (isLevelMode) {
                $form.append($("<input>", {
                    type: "hidden",
                    name: `results[${idx}].usedHint`,
                    value: usedHints[idx] ? "Y" : "N"
                }));

                if (usedHints[idx] && quiz.hintRemovedOption) {
                    $form.append($("<input>", {
                        type: "hidden",
                        name: `results[${idx}].hintRemovedOption`,
                        value: quiz.hintRemovedOption
                    }));
                }
            }
        });

        $form.submit();
    }

    /**
     * 퀴즈 종료 모달창 열기
     */
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
});