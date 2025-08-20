$(document).ready(function () {
    // 현재 퀴즈 인덱스 (0부터 시작)
    let currentQuizIndex = 0;

    // 사용자가 선택한 보기 번호를 저장하는 배열
    let selectedAnswers = [];

    // 자주 사용하는 DOM 요소 캐싱
    const $quizTotal = $("#quiz-total");
    const $quizCurrent = $("#quiz-current");
    const $quizQuestion = $("#quiz-question");
    const $quizOptions = $("#quiz-options");

    console.log("quizzes length =", quizzes.length); // 서버에서 전달된 퀴즈 데이터 확인용

    // 퀴즈 데이터 유효성 검사
    if (!Array.isArray(quizzes) || quizzes.length === 0) {
        alert("퀴즈를 불러오지 못했습니다.");
        return;
    }

    // 사용자가 선택한 답안을 저장할 배열 초기화
    selectedAnswers = new Array(quizzes.length).fill(null);

    // 총 문제 수 & 현재 문제 번호 표시
    $quizTotal.text(quizzes.length);
    $quizCurrent.text(1);

    // 첫 번째 문제 렌더링
    renderCurrentQuiz();

    /**
     * 현재 인덱스(currentQuizIndex)에 해당하는 문제와 보기를 화면에 출력
     */
    function renderCurrentQuiz() {
        const quiz = quizzes[currentQuizIndex];

        // 문제 번호와 질문 표시
        $quizCurrent.text(currentQuizIndex + 1);
        $quizQuestion
        .text(`Q${currentQuizIndex + 1}. ${quiz.question}`)
        .css("text-align", "center");

        // 이전 보기를 모두 비움
        $quizOptions.empty();

        // 보기 1~4 생성
        for (let i = 1; i <= 4; i++) {
            const optionText = quiz["option" + i]; // quiz.option1~4
            const isSelected = selectedAnswers[currentQuizIndex] === i; // 선택 여부 확인

            // 보기 버튼 생성
            const $btn = $(`
                 <button class="btn white" type="button" data-choice="${i}">
                <span class="choice-no">${i}.</span>
                <span class="choice-text">${optionText}</span>
                </button>
            `);


            // 이미 선택된 경우 selected 클래스 추가
            if (isSelected) {
                $btn.addClass("selected");
            }

            // 보기 클릭 시 선택 처리
            $btn.on("click", function () {
                selectedAnswers[currentQuizIndex] = i; // 선택한 번호 저장
                $quizOptions.find("button").removeClass("selected"); // 기존 선택 해제
                $(this).addClass("selected"); // 새 선택 적용
            });

            // 보기 영역에 버튼 추가
            $quizOptions.append($btn);
        }

        // 이전/다음 버튼 상태 및 텍스트 변경
        $("#prev-btn").prop("disabled", currentQuizIndex === 0);
        $("#next-btn").text(currentQuizIndex === quizzes.length - 1 ? "제출" : "다음");

        // 진행률 바 업데이트
        renderProgressBar();
    }

    /**
     * 진행률 바의 너비를 현재 문제 번호에 맞게 업데이트
     */
    function renderProgressBar() {
        const $experiencePoint = $('.timer_experience_point'); // 진행률 바 내부 채움 영역
        const progressPercent = ((currentQuizIndex + 1) / quizzes.length) * 100;
        $experiencePoint.css('width', progressPercent + '%');
    }

    /**
     * 이전 버튼 클릭 이벤트
     */
    $("#prev-btn").off("click").on("click", function () {
        if (currentQuizIndex > 0) {
            currentQuizIndex--;
            renderCurrentQuiz();
        }
    });

    /**
     * 다음 버튼 클릭 이벤트
     * - 마지막 문제일 경우 제출 확인 후 결과 전송
     */
    $("#next-btn").off("click").on("click", function () {
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
     * 힌트 버튼 클릭 이벤트
     * - 현재는 미구현 (알림창만 표시)
     */
    $("#hint-btn").off("click").on("click", function () {
        alert("힌트 기능은 추후 지원 예정입니다.");
    });

    /**
     * 결과 제출 처리
     * - 숨겨진 폼 필드에 각 문제의 결과 데이터를 추가하고 서버로 전송
     */
    function submitResults() {
        const $form = $("#quiz-result-form");
        $form.empty();

        // 문제별 결과 데이터 추가
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

        // 퀴즈 모드와 시간 데이터
        $form.append($("<input>", {
            type: "hidden",
            name: "quizMode",
            value: "level"
        }));
        $form.append($("<input>", {
            type: "hidden",
            name: "totalTimeSec",
            value: "0.0"
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

        // 폼 전송
        $form.submit();
    }
});
