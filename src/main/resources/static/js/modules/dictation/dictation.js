/*
    유지보수/가독성 향상을 위해 기능별 또는 구분지을 구성요소로 분리하여 관리 필요
    인덴트 맞춰 코딩 습관화 코드 파악/해석하는데 어려움있음
    
    정확하고 논리적인 구조로 사용자 여정에 대한 처리 로직 구상 필요
    1. 정답 확인 후에는 정답 입력란 제거 또는 입력안되도록 처리하고
        제출 버튼이 남은 기회와 상관없이 다음 버튼으로 변경
    2. 힌트 사용 후 새로고침 시 사용한 힌트 체크하여 힌트 사용한 내용 유지 필요
        (힌트 개수 차감되기 때문에)<-힌트 사용 시 즉시 힌트 개수 차감 처리해야하는 이유와도 같음
    3. 인풋에 require 사용 지양
        어차피 완벽한 제어가 불가능하여 원하는 처리가 되지 않으므로 자바스크립트로 제어
        (빈값 제출 불가 제한을 두려 require를 사용하였는데 공백은 제출 가능하여 
        자바스크립트로 직접 trim()과 != "" 와 같은 처리와 분기로 로직 구성해야 함)
 */
$(document).ready(() => {
    let questionInformation = {
        correctAnswer: '',   // 문제
        hint1: '',      // 띄어쓰기 힌트
        hint2: '',      // 초성 힌트
        hint3: ''       // 첫 글자 힌트
    };

    // 받아쓰기 관련 요소
    const $dictationSpeakingComponent = $("#dictation-speaking-component"); // 문제, 힌트 제공 컴포넌트
    const dictationQuestionNo = $dictationSpeakingComponent.data("question-no"); // 해당 문제 식별 번호
    const $hintsInfo = $dictationSpeakingComponent.find(".hints_info"); // 힌트 버튼 부모 요소
    const $userAnswer = $("#user-answer"); // 정답 입력 인풋 요소
    const $submitAnswer = $("#submitAnswer"); // 정답 제출 버튼 요소
    const $dictationForm = $(".dictation_form");  // 받아쓰기 진행화면
    const $hintCount = $("#hint-count");

    $userAnswer.focus();

    // 전역변수
    let isShowAnswer = false;   // 정답보기 사용 여부
    let hintNum  = null;    // 힌트 번호
    /*
        이벤트 핸들러 등록 및 실행 등 아래와 같이 사용이 가능하나 비동기 랜더링 방식의 
        페이지가 아니므로 랜더링이 완료된 후 리스터 등록이라면 간편히 제이쿼리를 활용하여 
        작성해도 무방함 그리고 아래 로직 중 실행되는 TTS 내장 객체는 
        유틸성으로 판별되어 따로 관리하여야 유지보수 용이함
     */
    // 스피커 버튼 클릭 시에만 서버에서 받아서 읽기 (DOM에 정답 안 심음)
    $(document).on("click", ".speaker_btn", async function () {
        console.log("스피커 버튼 클릭됨");
        // const no = $(this).attr("data-question-no"); // dictationQuestionNo 변수 선언으로 필요없음
        const data = await fetchQuestion(dictationQuestionNo);
        const text = data?.correctAnswer || "";
        if (!text) {
            console.warn("읽을 텍스트가 없습니다.");
            return;
        }

        // 초기화로 cancel이 아닌 토글 처럼 읽기 도중 다시 클릭 시 중단되도록 하는게 나아보임
        // speechSynthesis.cancel();
        // 현재 읽는 중인지 체크
        if (speechSynthesis.speaking) {
            // 읽고있는 중이라면 중단
            speechSynthesis.cancel();
            $dictationSpeakingComponent.removeClass("action");
            $userAnswer.focus();
            return;
        }

        // 안 읽고 있다면 새로 읽기
        const u = new SpeechSynthesisUtterance(text);
        u.lang = "ko-KR";

        /*
            읽기 끝났을 때 이벤트 핸들링 (상태 초기화)
            비돟기적으로 speak 실행 시 큐에 등록되어 실행되어
            아래와 같이 안전하게 큐에 등록된 객체의 종료 핸들러 실행 후 읽기 실행
            실질적으로 해당 이벤트를 실행이 아닌 리스너 등록으로 브라우저가 
            읽기를 안전하게 종료시킬 수 있도록 함
        */
        u.onend = () => {
            console.log("읽기 완료");
            $dictationSpeakingComponent.removeClass("action");
        };

        // 읽기 실행
        speechSynthesis.speak(u);
        $dictationSpeakingComponent.addClass("action");
        $userAnswer.focus(); // 사용자 편리성으로 자동으로 포커스 되도록 추가
    });

    let repeatNo = "";
    $(".repeat_btn").click(function () {
        const $btn = $(this);
        const questionNo = $btn.data("question-no");
        const text = $btn.data("question");

        const u = new SpeechSynthesisUtterance(text);
        u.lang = "ko-KR";

        u.onend = () => {
            console.log("읽기 완료");
        };

        // 현재 읽는 중인지 체크
        if (speechSynthesis.speaking) {
            // 읽고있는 중이라면 중단
            speechSynthesis.cancel();

            if (repeatNo === questionNo) {
                repeatNo = "";
                return;
            }
        }

        // 읽기 실행
        repeatNo = questionNo;
        speechSynthesis.speak(u);
    });

    /*
        함수명 통일화 필요 프로젝트 코드컨벤션 기반하여
        완벽한 통일이 아니더라도 기획안과의 어느정도 일치성을 보여야 함
     */
    async function fetchQuestion(dictationQuestionNo) {
        const usedHint = false;
        // 번호만 서버로 보내서 정답 받아오기 (DOM에 정답 노출 안 함)
        const res = await $.getJSON("/api/dictation/question",
            {dictationQuestionNo, usedHint});
        return res?.data || {};
    }

    // 모든 버튼 비활성화
    // 기능에 대한 내용 변경으로 함수명 수정 필요
    function disableAllHintButtons() {
        /*
            특정하는 요소 선택은 좋은 방법이지만(코드 컨벤션 맞지 않음)
            아래 선택자들에게만 적용할 클래스로 제어하는 편이 간편해 보임
            의도한 선택지라면 적극 추천
            단순 AI의 답변을 복/붙이라면 차선의 방법
            클래스 추가/제거로 제어 시 조작이 쉬움(개발자 모드에서 제거하면 힌트 추가 사용 가능)
            요소를 제거하는게 가능 좋아 보임(제일 간편한 방법)
        */
        // $("#AnswerBtn, #HintBtn1, #HintBtn2, #HintBtn3")
        // .addClass("disabled")

        $hintsInfo.fadeOut(150, function () {
            $(this).remove();
        }); // 힌트 버튼 부모 요소를 제거하여 완전 보이지 않도록 함
    }

    // function updateHintButton() {
    //     if (!$hintsInfo.length) return;
    //     const userHintCount = parseInt($hintCount.text());
    //
    //     if (userHintCount <= 0) {
    //         $hintsInfo.addClass("disabled");
    //     } else {
    //         $hintsInfo.removeClass("disabled");
    //     }
    // }
    //
    // updateHintButton();
    // 1. 비동기 요청으로 읽어줄 문제와 힌트목록을 요청하는 함수
    // 코드 컨벤션이 맞지않으며 의미가 명확하도록 수정
    // $("#correctAnswer").on("click", async function () {
    //     console.log("정답 보기 버튼 클릭됨");
    //
    //     // const $correctAnswerBtn = $(this);  // dictationQuestionNo 변수 선언으로 필요없음
    //
    //     if (!questionInformation.correctAnswer) {
    //         // const questionNo = $correctAnswerBtn.attr("data-question-no"); // dictationQuestionNo 변수 선언으로 필요없음
    //         const response = await getDictationQuestionApi(dictationQuestionNo);
    //         showAnswerInSquares(response.correctAnswer);
    //     } else {
    //         showAnswerInSquares(questionInformation.correctAnswer);
    //     }
    //
    //     // 정답 보기 사용시 '정답 입력' 부분 작성 불가
    //     // [/submit-answer] 부분 @RequestParam("userAnswer") 값을 넘겨야 하므로 값은 넘기고(db에는 저장x)
    //     // '정답 입력' 부분 비활성화
    //
    //     //**** 정호 잘 읽어라
    //     // 이런식으로 속성만 바꾸는 대처는 제발 우리 앱을 버그로 써먹어라 라는거야
    //     // $('#user-answer').prop('readonly', true).addClass('disabled');
    //
    //     $userAnswer.remove(); // 조작 원천 차단
    //     $submitAnswer.text("다음"); // 텍스트 변경
    //
    //     disableAllHintButtons();
    //     isShowAnswer = true;
    // });

    // 건너띄기 버튼 클릭시 isShowAnswer = true
    $("#skip-btn").on("click", function (e) {
        e.preventDefault();
        console.log("건너뛰기 버튼 클릭됨")
        isShowAnswer = true;
        $dictationForm.submit();
    })

    // 정답 보기 버튼 클릭 시 showAnswer hidden 추가
    $dictationForm.submit(function (e) {
        e.preventDefault();
        const $this = $(this);

        // showAnswer hidden 추가
        $this.append('<input type="hidden" name="showAnswer" value="' + (isShowAnswer ? '1' : '0') + '">');

        // 그대로 submit
        this.submit();
    });

    // 코드 컨벤션이 맞지않아 수정
    // $("#HintBtn1").on("click", async function () {
    $("#hintBtn1").on("click", async function () {
        console.log("띄어 쓰기 힌트 버튼 클릭됨")

        // const $hint1Btn = $(this); // dictationQuestionNo 변수 선언으로 필요없음

        if (!questionInformation.hint1) {
            // const questionNo = $hint1Btn.attr("data-question-no"); // dictationQuestionNo 변수 선언으로 필요없음
            const response = await getDictationQuestionApi(dictationQuestionNo, true);
            stayUsedHintNum($(this));
            showAnswerInSquares(response.hint1);
        } else {
            showAnswerInSquares(questionInformation.hint1);
        }
        disableAllHintButtons();
        $userAnswer.focus(); // 사용자 편리성으로 자동으로 포커스 되도록 추가
    });

    // 코드 컨벤션이 맞지않아 수정
    // $("#HintBtn2").on("click", async function () {
    $("#hintBtn2").on("click", async function () {
        console.log("초성나열 힌트 버튼 클릭됨")

        // const $hint2Btn = $(this); // dictationQuestionNo 변수 선언으로 필요없음

        if (!questionInformation.hint2) {
            // const questionNo = $hint2Btn.attr("data-question-no"); // dictationQuestionNo 변수 선언으로 필요없음
            const response = await getDictationQuestionApi(dictationQuestionNo, true);
            stayUsedHintNum($(this));
            showAnswerInSquares(response.hint2);
        } else {
            showAnswerInSquares(questionInformation.hint2);
        }
        disableAllHintButtons();
        $userAnswer.focus(); // 사용자 편리성으로 자동으로 포커스 되도록 추가
    });

    // 코드 컨벤션이 맞지않아 수정
    // $("#HintBtn3").on("click", async function () {
    $("#hintBtn3").on("click", async function () {
        console.log("첫 글자 힌트 버튼 클릭됨")

        // const $hint3Btn = $(this); // dictationQuestionNo 변수 선언으로 필요없음

        if (!questionInformation.hint3) {
            // const questionNo = $hint3Btn.attr("data-question-no"); // dictationQuestionNo 변수 선언으로 필요없음
            const response = await getDictationQuestionApi(dictationQuestionNo, true);
            stayUsedHintNum($(this));
            showAnswerInSquares(response.hint3);
        } else {
            showAnswerInSquares(questionInformation.hint3);
        }
        disableAllHintButtons();
        $userAnswer.focus(); // 사용자 편리성으로 자동으로 포커스 되도록 추가
    });

    // 힌트 사용 시 새로 고침해도 같은 hintNum으로 힌트유지
    function stayUsedHintNum($hintElement) {
        const hintnum = $hintElement.attr("id") === "hintBtn1" ? 1 : (this.id === "hintBtn2" ? 2 : 3);

        $dictationForm.append('<input type="hidden" name="hintNum" value="' + hintnum + '">');
        $dictationForm.append('<input type="hidden" name="showAnswer" value="0">');

        $dictationForm.submit();
    }

    // $("#hintBtn1, #hintBtn2, #hintBtn3").on("click", async function () {
    //     const hintnum = this.id === "hintBtn1" ? 1 : (this.id === "hintBtn2" ? 2 : 3);
    //
    //     $dictationForm.append('<input type="hidden" name="hintNum" value="' + hintnum + '">');
    //     $dictationForm.append('<input type="hidden" name="showAnswer" value="0">');
    //
    //     // $("#main-content") 부분 e.preventDefault()를 건너뛰고, 브라우저의 기본 폼 제출을 직접 실행시키기 위해 추가
    //     $dictationForm[0].submit();
    // });

    // 2. 문제 번호를 인자로 넘겨 getDictationQuestionApi 함수 실행
    /*
        들여쓰기 확인하다보면 괄호가 잘못된걸 파악할 수 있었을 텐데
        이 부분의 오작성으로 코드 파악어려워짐
     */
    // 힌트 사용시 호출 함수
    async function getDictationQuestionApi(dictationQuestionNo, usedHint) {
        console.log("요청 시작: /api/dictation/question", dictationQuestionNo);
        try {
            const response = await $.ajax({
                url: '/api/dictation/question',
                method: 'GET',
                data: {dictationQuestionNo, usedHint},
                dataType: 'json'
            });
            questionInformation = response.data;
            return questionInformation;

        } catch (err) {
            console.error("에러 발생", err);
        }
    } // 정상적으로 닫히도록 추가함
    // } // 이게 왜 있는건지
    // }); // 함수 선언인데 "}"로 끝나지 않고 "});"로 끝나는 이유는?? 이렇게 작성한 의도가 무엇인지
    /*
        현재 위의 코드 문제로 
        $(document).ready(() => { <- 이 부분에 대하여 위 로직에서 닫혀 버림
        아래 함수 선언 부분부터는 안전하지 않은 상태로 간헐적 오류 발생률을 높임
        ready 함수 내 선언된 변수 및 함수 등 외부에서 사용되지 못함
     */

    // 사각형 안에 단어 하나씩 입력하는 로직
    // 정확한 함수명과 매개변수 명 필요 해당 함수는 힌트 내용을 표시하는것으로 파악됨 답변이 아님
    /*
        아래 함수 작성에도 괄호 오류 있음
     */
    function showAnswerInSquares(answer) {
        /*
            레이아웃 수정으로 인한 로직 수정 및 기존 로직 최적화
            필히 확인하여 왜 변경하였는지 파악하기 바람
         */
        // 힌트 내용 표시 요소 찾기
        let $dictationHintInfo = $dictationSpeakingComponent.find(
            ".dictation_hint_info");

        let hintContent = ""; // 힌트 내용

        // 힌트 내용을 분할하여 표시할 내용 입력
        answer.split("").forEach(function (hintInfo) {
            hintContent += `<span class="square">${hintInfo}</span>`;
        });

        // 힌트 내용 세팅
        const $hintInfoSetting = $("<div>").addClass("square_list").html(
            hintContent);

        if ($dictationHintInfo.length) {
            // 힌트 내용 표시할 부모 요소가 있을 경우
            $dictationHintInfo.empty(); // 내용 제거
            $dictationHintInfo.append($hintInfoSetting); // 추가
        } else {
            // 힌트 내용 표시할 부모 요소가 없을 경우
            // 힌트 내용 표시할 부모 요소 생성 후 세팅하여 상위 요소에 추가
            $dictationHintInfo = $("<div>").addClass(
                "dictation_hint_info").html($hintInfoSetting);
            $dictationSpeakingComponent.append($dictationHintInfo);
        }

        // let $container = $(".square_info");
        // $container.empty(); // 기존 내용 삭제
        //
        // // 정답 문자열을 한 글자씩 나눔
        // answer.split("").forEach(function (ch) {
        //     // <span></span> 구문 안 사각형에 text(ch)로 한 글자씩 담기(<span class="square">가</span> 생성)
        //     let $span = $("<span>").addClass("square").text(ch);
        //     //class=suqare_inf 내부 끝에 <span>가</span> 추가
        //     $container.append($span);
        // });
        //
        // // /js/modules/dictation/dictation.js
        // console.log("[dictation.js] loaded");
    }

    // $(".hint_btn").on("click", function () {
    //     // const dictationQuestionNo = $(this).attr("data-question-no"); // dictationQuestionNo 변수 선언으로 필요없음
    //
    //     // 사용하지 않은 불필요한 변수는 왜 선언해놓았는지?
    //     // 관련된 데이터는 head에 자동으로 ajax가 실행되면 주입되도록 설정해놨다고 공유했음
    //     // const token = $('meta[name="_csrf"]').attr('content');
    //     // const header = $('meta[name="_csrf_header"]').attr('content');
    //
    //     $.ajax({
    //         url: "/dictation/use-hint",
    //         type: "POST",
    //         dataType: "json",
    //         success: function (res) {
    //             if (res.success) {
    //                 console.log(res.message); // "성공"
    //             }
    //         }
    //     });
    // });
// }; // 결국 전체 적으로 코드가 꼬여버림

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

    // 서버 리다이렉트 후 hidden으로 숨겨둔 (#flash-correct / #flash-second-fail)를 읽어
    // 알림을 표시 (val값이 '1'일때 활성)
    flashAlerts();

    function flashAlerts() {

        const $flags = $('#main-content');
        if (!$flags.length) return;

        const correct    = $flags.data('correct');
        const secondFail = $flags.data('secondFail');

        if (correct) {
            alert('정답입니다.\n다음 문제로 이동합니다.');
            // 2번째 시도 후 정답일 경우 둘 다 활성화되기 때문에 이 알림 한 번만 띄우도록 함
            return; // 동시 세팅이면 정답 알림만 1회
        }
        if (secondFail) {
            alert('오답입니다.\n다음 문제로 이동합니다.');
        }
    }

}); // 정상적으로 닫히도록 추가함

