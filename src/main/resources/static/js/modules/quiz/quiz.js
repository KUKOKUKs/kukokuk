import {apiUseHint} from './quiz-api.js';
import {
    renderQuizQuestion,
    renderQuizOptions,
    renderProgressBar,
    renderTimer,
    startTimerAnimation,
    renderHintButton,
    renderNavigationButtons
} from './quiz-renderer.js';
import {
    handleOptionSelect,
    handleHintRemoveOption,
    animateHintCount
} from './quiz-handler.js';
import {
    getRandomWrongOption,
    buildQuizResultData
} from './quiz-util.js';

$(document).ready(function () {
    // 데이터 검증
    if (typeof quizMode === 'undefined') {
        console.error("quizMode 변수가 정의되지 않았습니다.");
        return;
    }

    if (!quizzes || !Array.isArray(quizzes) || quizzes.length === 0) {
        console.error("퀴즈 데이터를 불러오지 못했습니다.");
        return;
    }

    // 퀴즈 상태 관리
    const quizState = {
        currentIndex: 0,
        selectedAnswers: [],
        usedHints: [],
        timerInterval: null,
        timeLeft: 10,
        startTime: Date.now(),
        isSpeedMode: quizMode === "speed",
        isLevelMode: quizMode === "level"
    };

    // Level 모드 초기화
    if (quizState.isLevelMode) {
        quizState.usedHints = new Array(quizzes.length).fill(false);
    }

    /**
     * 현재 퀴즈 렌더링
     */
    function renderCurrentQuiz() {
        const quiz = quizzes[quizState.currentIndex];

        // 문제와 보기 렌더링
        renderQuizQuestion(quiz, quizState.currentIndex, quizzes.length);
        renderQuizOptions(
            quiz,
            quizState.selectedAnswers[quizState.currentIndex],
            quizState.usedHints[quizState.currentIndex]
        );

        // 모드별 추가 렌더링
        if (quizState.isLevelMode) {
            renderProgressBar(quizState.currentIndex, quizzes.length);
            renderHintButton(
                parseInt($("#hint-count").text()),
                quizState.usedHints[quizState.currentIndex]
            );
            renderNavigationButtons(quizState.currentIndex, quizzes.length);
        } else if (quizState.isSpeedMode) {
            startQuizTimer();
        }
    }

    /**
     * Speed 모드 타이머 시작
     */
    function startQuizTimer() {
        if (!quizState.isSpeedMode) return;

        clearInterval(quizState.timerInterval);
        quizState.timeLeft = 10;

        // 타이머 UI 초기화
        renderTimer(quizState.timeLeft, 10);
        startTimerAnimation(10);

        // 타이머 카운트다운
        quizState.timerInterval = setInterval(() => {
            quizState.timeLeft--;
            renderTimer(quizState.timeLeft, 10);

            if (quizState.timeLeft <= 0) {
                clearInterval(quizState.timerInterval);
                moveToNextQuiz();
            }
        }, 1000);
    }

    /**
     * 다음 문제로 이동
     */
    function moveToNextQuiz() {
        quizState.currentIndex++;

        if (quizState.currentIndex < quizzes.length) {
            renderCurrentQuiz();
        } else {
            submitQuizResults();
        }
    }

    /**
     * 퀴즈 결과 제출
     */
    async function submitQuizResults() {
        console.log("퀴즈 완료! 결과 제출");

        const totalTimeSec = quizState.isSpeedMode
            ? Math.floor((Date.now() - quizState.startTime) / 1000)
            : 0;

        const resultData = buildQuizResultData(
            quizzes,
            quizState.selectedAnswers,
            quizState.usedHints,
            quizMode,
            totalTimeSec
        );

        // 폼 제출 방식으로 변경 (기존 로직 유지)
        const $form = $("#quiz-result-form");
        $form.empty();

        // 데이터를 hidden input으로 추가
        Object.keys(resultData).forEach(key => {
            if (key === 'results') {
                resultData.results.forEach((result, idx) => {
                    Object.keys(result).forEach(field => {
                        $form.append($("<input>", {
                            type: "hidden",
                            name: `results[${idx}].${field}`,
                            value: result[field]
                        }));
                    });
                });
            } else {
                $form.append($("<input>", {
                    type: "hidden",
                    name: key,
                    value: resultData[key]
                }));
            }
        });

        // CSRF 토큰 추가
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

    // ========== 이벤트 핸들러 ==========

    // 보기 선택
    $(document).on("click", ".option_btns:not(.hint-removed)", function () {
        const choiceNumber = handleOptionSelect($(this));
        quizState.selectedAnswers[quizState.currentIndex] = choiceNumber;

        // Speed 모드: 선택 즉시 다음 문제
        if (quizState.isSpeedMode) {
            clearInterval(quizState.timerInterval);
            moveToNextQuiz();
        }
    });

    // 힌트 사용 (Level 모드)
    $("#hint-btn").off("click").on("click", async function () {
        if (!quizState.isLevelMode) return;

        const userHintCount = parseInt($("#hint-count").text());

        if (userHintCount <= 0) {
            alert("사용 가능한 힌트가 없습니다.");
            return;
        }

        if (quizState.usedHints[quizState.currentIndex]) {
            alert("이미 힌트를 사용한 문제입니다.");
            return;
        }

        if (!confirm("힌트를 사용하시겠습니까? (1개 차감)")) {
            return;
        }

        const currentQuiz = quizzes[quizState.currentIndex];
        const removedOption = getRandomWrongOption(currentQuiz);

        // UI 업데이트
        const wasSelected = handleHintRemoveOption(removedOption);
        if (wasSelected) {
            quizState.selectedAnswers[quizState.currentIndex] = null;
        }

        // 상태 업데이트
        quizState.usedHints[quizState.currentIndex] = true;
        currentQuiz.hintRemovedOption = removedOption;

        try {
            // API 호출
            const response = await apiUseHint(quizState.currentIndex, removedOption);

            if (response && response.remainingHints !== undefined) {
                $("#hint-count").text(response.remainingHints);
            } else {
                $("#hint-count").text(userHintCount - 1);
            }

            animateHintCount($("#hint-count"));
            renderHintButton(
                parseInt($("#hint-count").text()),
                quizState.usedHints[quizState.currentIndex]
            );

        } catch (error) {
            console.error("힌트 사용 실패:", error);
            alert("힌트 사용에 실패했습니다.");
        }
    });

    // 이전 버튼 (Level 모드)
    $("#prev-btn").off("click").on("click", function () {
        if (!quizState.isLevelMode || quizState.currentIndex <= 0) return;

        quizState.currentIndex--;
        renderCurrentQuiz();
    });

    // 다음/제출 버튼 (Level 모드)
    $("#next-btn").off("click").on("click", function () {
        if (!quizState.isLevelMode) return;

        if (quizState.currentIndex < quizzes.length - 1) {
            quizState.currentIndex++;
            renderCurrentQuiz();
        } else {
            if (confirm("퀴즈를 제출하시겠습니까?")) {
                submitQuizResults();
            }
        }
    });

    // 퀴즈 종료 모달
    $(".quiz_end_btn").click(function () {
        const $modal = $("#modal-quiz-exit");
        if ($modal.length) {
            $modal.show();
            setTimeout(() => $modal.addClass("open"), 10);
        }
    });

    // 초기 렌더링
    renderCurrentQuiz();
});