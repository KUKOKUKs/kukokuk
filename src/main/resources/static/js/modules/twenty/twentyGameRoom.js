/**
 * 함수 및 이벤트 리스너에 사용된 HTML 요소들
 * 전역 변수로 선언
 * @type {jQuery|HTMLElement|*}
 */
const pageContainer = $(".page_container");
const currentUserNo = pageContainer.data("user-no");
const StringroomNo = pageContainer.data("room-no");
const currentRoomNo = parseInt(StringroomNo);
const wsUrl = pageContainer.data("ws-url");
const ACCESS_TOKEN = pageContainer.data("access-token");

let stompClient = null;
let turnTimer = null;

// 교사 버튼
let $teacherObtn = $('#teacher-o-btn');
let $teacherXbtn = $('#teacher-x-btn');
let $teacherEndBtn = $("#teacher-end-btn");
let $teacherStartBtn = $("#teacher-start-btn");

// 학생 버튼
let $sendQuestionBtn = $("#send-question");     // 질문 전송 버튼
let $questionInput = $("#question-input");      // 질문 입력풋
let $sendAnswerBtn = $("#send-answer");         // 정답 전송 버튼
let $answerInput = $("#answer-input");          // 정답 입력풋
let $raiseHandBtn = $("#raise-hand");           // 손들기 버튼

let visualTimer = null; // 화면 타이머 ID를 저장할 전역 변수

/**
 * 웹소켓 연결 및 구독 관리 함수
 */
function connectWebSocket() {
    console.log("ws: ", wsUrl);
    const socket = new SockJS('http://localhost:8081/ws?token=' +ACCESS_TOKEN);
    stompClient = Stomp.over(socket);
    /**
     * 웹소켓 연결
     */
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        // 구독 관리 코드들
        /**
         * 참여자 입장 시, 실시간 UI 변화
         */
        stompClient.subscribe(`/topic/participants/${currentRoomNo}`,
            function (result) {
                const data = JSON.parse(result.body);
                //data => list, roomStatus
                updateParticipantList(data.list);
                updateBtnByRoomStatus(data);
            });

        /**
         * 게임 시작 버튼을 눌렀을 경우 UI 변화
         */
        stompClient.subscribe(`/topic/gameStart/${currentRoomNo}`,
            function (result) {
                const data = JSON.parse(result.body);
                console.log("data: ", data);
                appendBoardLine(data);
                updateBtnByRoomStatus(data)
            });

        /**
         * 손들기 버튼을 눌렀을 경우 UI 변화
         */
        stompClient.subscribe(`/topic/raisehand/${currentRoomNo}`,
            function (result) {
                const data = JSON.parse(result.body);
                console.log("data: ", data);
                $("#turn-holder-name").text(data.name);
                $("#turn-info-panel").show();
                startVisualTimer(data.time);

                if(data.msgCnt == null) { // 질문 횟수가 19개 미만일 때는, 자유롭게 입력
                    updateBtnByRoomStatus(data)
                } else {                  // 질문 횟수가 19개 이상일 때, 시스템 메세지 + 정답 입력풋만 활성화
                    appendBoardLine(data);
                    $answerInput.prop("disabled", false);
                    $sendAnswerBtn.prop("disabled", false);
                }
            });

        /**
         * 40초 제한 시간 넘겼을 경우, UI 변화
         */
        stompClient.subscribe(`/topic/turnTimeout/${currentRoomNo}`,
            function (result) {
            const data = JSON.parse(result.body);
            updateBtnByRoomStatus(data);
            });

        /**
         * 학생이 질문&정답 입력 후 전송 버튼 눌렀을 경우 UI 변화
         */
        stompClient.subscribe(`/topic/sendStdMsg/${currentRoomNo}`,
            function (result) {
            let data1 = JSON.parse(result.body);
            console.log("data: ", data1);
                updateBtnByRoomStatus(data1);
                appendBoardLine(data1);
                stopVisualTimer(); // UI에서 타이머도 종료

            });

        /**
         * 교사가, 게임 종료 버튼 or 서버 끊김 or 웹 브라우저 탭 닫을 경우
         * 모든 참여자 group 페이지로 이동.
         */
        stompClient.subscribe(`/topic/TeacherDisconnect`,
            function (result) {
                window.location.href = '/group';
            });

        /**
         * 교사 O&X 버튼을 눌렀을 경우,
         */
        stompClient.subscribe(`/topic/TeacherResponce`,
            function (result) {
            const data = JSON.parse(result.body);

            //게임이 끝난경우
            if(data.system != null) {
                appendBoardLine(data);
                $teacherEndBtn.prop('disabled', false);
                toggleGlow($teacherEndBtn, true);
                return;
            }else { // 게임을 재개 해야하는 경우
                updateBtnByRoomStatus(data);
                renderMessageList(data.msgList);
            }


            });

        /**
         * 입장할 때마다, 누가 들어왔는지 신호 보내는 함수
         */
        stompClient.send(`/app/join/${currentRoomNo}`, {}, JSON.stringify({}));
    });
}

/**
 * 모든 HTML 요소가 만들어지고 나서, 아래 함수 및 이벤트 리스너들 실행.
 */
$(function () {
    /**
     * 웹소켓 연결 함수 실행.
     */
    connectWebSocket();

    //이 게임방의 모든 채팅 이력을 보여주는 코드 - ajax


    // --- 교사 기능 ---

    /**
     * 교사가 O 버튼을 눌렀을 때
     */
    $teacherObtn.click(function (e) {
        e.preventDefault();
        const data = {
            roomNo : currentRoomNo,
            response : "Y"
        };
        stompClient.send(`/app/teacherResponse`,{}, JSON.stringify(data));

    });

    /**
     * 교사가 X 버튼 눌렀을 때
     */
    $teacherXbtn.click(function (e) {

        const data = {
            roomNo : currentRoomNo,
            response : "N"
        };
        stompClient.send(`/app/teacherResponse`,{}, JSON.stringify(data));
    });

    /**
     * 교사가 게임 시작 버튼 눌렀을 때
     */
    $teacherStartBtn.click(function () {
        stompClient.send(`/app/gameStart/${currentRoomNo}`, {},
            JSON.stringify({}));
    });

    /**
     * 교사가 게임 종료 버튼 눌렀을 때.
     */
    $teacherEndBtn.click(function () {
        let sendData = {
            roomNo: currentRoomNo,
        };
        let response = $.ajax({
            url: '/api/twenty/gameOver',
            type: 'POST',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(sendData),
        });
        window.location.href = '/group';
    });

    // --- 학생 기능 ---
    /**
     * 학생이 손들기 버튼 눌렀을 때
     */
    $raiseHandBtn.click(function (e) {
        e.preventDefault();
        stompClient.send(`/app/raisehand/${currentRoomNo}`, {},
            JSON.stringify({}));
    });

    /**
     * 학생이 질문 입력 후 질문 전송 버튼을 눌렀을 때
     */
    $sendQuestionBtn.click(function (e) {
        e.preventDefault();

         let content = $questionInput.val().trim();
         if(content == null) {return;}

         let msgData = {
             userNo : currentUserNo,
             roomNo : currentRoomNo,
             type : "Q",
             content : content
         };
         stompClient.send("/app/sendStdMsg",{},JSON.stringify(msgData));
        $questionInput.val('');
    });

    /**
     * 학생이 정답 입력 후 정답 전송 버튼 눌렀을 때
     */
    $sendAnswerBtn.click(function (e) {
        e.preventDefault();

        let content = $answerInput.val().trim();
        if(content == null) {return;}

        let msgData = {
            userNo : currentUserNo,
            roomNo : currentRoomNo,
            type : "A",
            content : content
        };
        stompClient.send("/app/sendStdMsg",{},JSON.stringify(msgData));
        $answerInput.val('');
    });

    // 수정 예정
    function handleSendClick(event) {
        const isQuestion = event.target.id === 'send-question';
        const input = document.getElementById(
            isQuestion ? 'question-input' : 'answer-input');
        const text = input.value.trim();

        if (text) {
            const msg = {
                roomId: currentRoomNo,
                content: text
            };
            stompClient.send(`/app/chatSend/${currentRoomNo}`, {},
                JSON.stringify(msg));
            input.value = '';
        }
        stompClient.send(`/app/turnOff/${currentRoomNo}`, {},
            JSON.stringify({}));
    }
});

// --- UI 상태 변경 함수들 ---
/**
 * 참여자의 리스트를 가지고,
 * 참여자의 상태에 따라 스타일이 달라진다.
 * @param userList 참여자 리스트
 */
function updateParticipantList(userList) {
    console.log("userList2: ", userList);
    let $list = $("#participants-list");
    $list.empty();

    for (let i of userList) {
        let statusClass = '';
        if (i.status == "JOINED") {
            statusClass = 'participants_user-join';
        } else {
            statusClass = 'participants_user';
        }

        // 2. data-user-no 속성값에 따옴표 추가
        let injectHtml = `<div class="${statusClass}"
                           data-user-no="${i.userNo}">
          ${i.nickName}
        </div>`;

        $list.append(injectHtml);
    }
}

/**
 * 채팅 메세지 표시
 * @param message
 */
function appendBoardLine(data) {
    const $board = $("#board-area");
    const $firstMessage = $(".system_message");

    // 1️⃣ 시스템 메시지일 경우 (예: "스무고개를 시작합니다.")
    if (data.system != null) {
        $firstMessage.text(data.system);
        return;
    }

    // 2️⃣ 기본 스타일 변수 초기화
    let lineClass = '';
    let senderHtml = '';
    let bubbleClass = '';

    // 3️⃣ 본인/타인 구분
    if (data.userNo == currentUserNo) {
        // 내 메시지 → 오른쪽 정렬
        lineClass = 'my-message';
    } else {
        // 다른 사람 메시지 → 왼쪽 정렬 + 닉네임 표시
        lineClass = 'other-message';
        senderHtml = `<div class="sender">${data.nickName}</div>`;
    }

    // 4️⃣ 메시지 타입(Q/A)에 따른 색상 구분
    switch (data.type) {
        case 'Q':
            bubbleClass = 'question-bubble';
            break;
        case 'A':
            bubbleClass = 'answer-bubble';
            break;
        default:
            bubbleClass = 'normal-bubble';
            break;
    }

    // 5️⃣ 개행 처리
    const contentHtml = data.content ? data.content.replace(/\n/g, '<br>') : '';

    // 6️⃣ 최종 HTML 조립
    const newLineHtml = `
        <div class="message-line ${lineClass}" data-user-no="${data.userNo}" data-log-no="${data.logNo}">
            ${senderHtml}
            <div class="message-bubble ${bubbleClass}">
                ${contentHtml}
            </div>
        </div>
    `;

    // 7️⃣ DOM 추가
    $board.append(newLineHtml);

    // 8️⃣ 스크롤 맨 아래로 이동
    $board.scrollTop = $board.scrollHeight;
}

/**
 * 게임방의 상태에 따른 버튼 변화
 */
function updateBtnByRoomStatus(data) {
    let roomStatus = data.roomStatus;
    let winnerNo = data.userNo != null ? data.userNo : null;

    // 모든 버튼 초기화 + glow 제거
    [
        $teacherObtn, $teacherXbtn, $teacherEndBtn, $teacherStartBtn,
        $sendQuestionBtn, $sendAnswerBtn, $raiseHandBtn
    ].forEach(btn => {
        btn.prop('disabled', true);
        toggleGlow(btn, false);
    });

    $questionInput.prop('disabled', true);
    $answerInput.prop('disabled', true);

    // 상태에 따른 활성화 처리
    switch (roomStatus) {
        case 'WAITING':
            $teacherStartBtn.prop('disabled', false);
            toggleGlow($teacherStartBtn, true);
            break;
        case 'IN_PROGRESS':
            $raiseHandBtn.prop('disabled', false);
            toggleGlow($raiseHandBtn, true);
            break;
        case 'AWAITING_INPUT':
            if (currentUserNo == winnerNo) {
                $sendQuestionBtn.prop('disabled', false);
                $sendAnswerBtn.prop('disabled', false);
                $questionInput.prop('disabled', false);
                $answerInput.prop('disabled', false);
                toggleGlow($sendQuestionBtn, true);
                toggleGlow($sendAnswerBtn, true);
            }
            break;
        case 'AWAITING_RESPONSE':
            $teacherObtn.prop('disabled', false);
            $teacherXbtn.prop('disabled', false);
            toggleGlow($teacherObtn, true);
            toggleGlow($teacherXbtn, true);
            break;
        default:
            break;
    }
}

/**
 * 버튼의 glow 효과 부여
 * enbaled가 true일 때 빛나는 효과 추가
 * false일 때는 CSS 빛나는 효과 삭제
 * @param $el
 * @param enabled
 */
function toggleGlow($el, enabled) {
    if (enabled) {
        $el.addClass('enabled-glow');
    } else {
        $el.removeClass('enabled-glow');
    }
}

/**
 * 타이머 움직이게 하는 메소드
 * @param time
 */
function startVisualTimer(time) {
    // 1. 이전에 실행되던 타이머가 있다면 완전히 제거
    if (visualTimer) {
        clearInterval(visualTimer);
    }

    let remainingTime = time;

    // 2. 화면에 초기 시간을 먼저 표시
    $('#timer-display').text(remainingTime);

    // 3. 1초(1000ms)마다 실행되는 인터벌 설정
    visualTimer = setInterval(() => {
        remainingTime--; // 시간을 1초 줄임
        $('#timer-display').text(remainingTime);

        // 4. 화면 타이머가 0초가 되면 인터벌 종료
        if (remainingTime <= 0) {
            stopVisualTimer();
        }
    }, 1000);
}

/**
 * 타이머 종료 메소드
 */
function stopVisualTimer() {
    if (visualTimer) {
        clearInterval(visualTimer);
    }
    // 필요하다면 타이머 패널을 다시 숨깁니다.
    $('#turn-info-panel').hide();
}

/**
 * 서버에서 받은 메시지 리스트를 화면에 렌더링하는 함수
 * @param {Array} messageList - 서버에서 내려준 메시지 배열
 */
function renderMessageList(messageList) {
    const $board = $("#board-area");
    $board.empty(); // 이전 채팅 내용 초기화 (필요 시 제거 가능)

    if (!Array.isArray(messageList) || messageList.length === 0) {
        console.log("표시할 메시지가 없습니다.");
        return;
    }

    // 서버에서 받은 메시지를 순서대로 화면에 출력
    messageList.forEach((msg) => {
        appendBoardLine(msg);
    });

    // 모든 메시지를 렌더링한 후 스크롤을 맨 아래로 이동
    $board.scrollTop($board[0].scrollHeight);
}
