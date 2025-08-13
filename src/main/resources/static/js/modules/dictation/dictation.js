$(document).ready(() => {
    let questionInformation = {
        correctAnswer: '',   // 문제
        hint1: '',      // 띄어쓰기 힌트
        hint2: '',      // 초성 힌트
        hint3: ''       // 첫 글자 힌트
    };

    // 스피커 버튼 클릭 시에만 서버에서 받아서 읽기 (DOM에 정답 안 심음)
    $(document).on("click", ".question_title_info", async function () {
        const no = $(this).attr("data-question-no");
        const data = await fetchQuestion(no);
        const text = data?.correctAnswer || "";
        if (!text) {
            console.warn("읽을 텍스트가 없습니다.");
            return;
        }
        speechSynthesis.cancel();
        const u = new SpeechSynthesisUtterance(text);
        u.lang = "ko-KR";
        speechSynthesis.speak(u);
    });

    async function fetchQuestion(dictationQuestionNo) {
        // 번호만 서버로 보내서 정답 받아오기 (DOM에 정답 노출 안 함)
        const res = await $.getJSON("/api/dictation/question", { dictationQuestionNo });
        return res?.data || {};
    }

    // 모든 버튼 비활성화
    function disableAllHintButtons() {
        $("#AnswerBtn, #HintBtn1, #HintBtn2, #HintBtn3")
        .addClass("disabled");
    }

    // 1. 비동기 요청으로 읽어줄 문제와 힌트목록을 요청하는 함수
    $("#AnswerBtn").on("click", async function() {
        console.log("정답 보기 버튼 클릭됨");

        const $correctAnswerBtn = $(this);

        if (!questionInformation.correctAnswer) {
            const questionNo = $correctAnswerBtn.attr("data-question-no");
            const response = await getDictationQuestionApi(questionNo);
            showAnswerInSquares(response.correctAnswer);
        } else {
            showAnswerInSquares(questionInformation.correctAnswer);
        }
        disableAllHintButtons();
    });

    $("#HintBtn1").on("click", async function(){
        console.log("띄어 쓰기 힌트 버튼 클릭됨")

        const $hint1Btn = $(this);

        if(!questionInformation.hint1) {
            const questionNo = $hint1Btn.attr("data-question-no");
            const response = await getDictationQuestionApi(questionNo);
            showAnswerInSquares(response.hint1);
        } else {
            showAnswerInSquares(questionInformation.hint1);
        }
        disableAllHintButtons();
    });

    $("#HintBtn2").on("click", async function(){
        console.log("초성나열 힌트 버튼 클릭됨")

        const $hint2Btn = $(this);

        if(!questionInformation.hint2) {
            const questionNo = $hint2Btn.attr("data-question-no");
            const response = await getDictationQuestionApi(questionNo);
            showAnswerInSquares(response.hint2);
        } else {
            showAnswerInSquares(questionInformation.hint2);
        }
        disableAllHintButtons();
    });

    $("#HintBtn3").on("click", async function(){
        console.log("첫 글자 힌트 버튼 클릭됨")

        const $hint3Btn = $(this);

        if(!questionInformation.hint3) {
            const questionNo = $hint3Btn.attr("data-question-no");
            const response = await getDictationQuestionApi(questionNo);
            showAnswerInSquares(response.hint3);
        } else {
            showAnswerInSquares(questionInformation.hint3);
        }
        disableAllHintButtons();
    });

    // 2. 문제 번호를 인자로 넘겨 getDictationQuestionApi 함수 실행
    async function getDictationQuestionApi(dictationQuestionNo) {
        console.log("요청 시작: /api/dictation/question", dictationQuestionNo);

        try {
            const response = await $.ajax({
                url: '/api/dictation/question',
                method: 'GET',
                data: {dictationQuestionNo},
                dataType: 'json'
            });
            questionInformation = response.data;
            return questionInformation;

        } catch (err) {
            console.error("에러 발생", err);
        }
    }
});

    // 사각형 안에 단어 하나씩 입력하는 로직
    function showAnswerInSquares(answer) {
        let $container = $(".square_info");
        $container.empty(); // 기존 내용 삭제

        // 정답 문자열을 한 글자씩 나눔
        answer.split("").forEach(function(ch) {
            // <span></span> 구문 안 사각형에 text(ch)로 한 글자씩 담기(<span class="square">가</span> 생성)
            let $span = $("<span>").addClass("square").text(ch);
            //class=suqare_inf 내부 끝에 <span>가</span> 추가
            $container.append($span);
        });

        // /js/modules/dictation/dictation.js
        console.log("[dictation.js] loaded");


    };