/**
 * 전역 요소 캐시
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
let $questionInput = $("#question-input");      // 질문 입력폼
let $sendAnswerBtn = $("#send-answer");         // 정답 전송 버튼
let $answerInput = $("#answer-input");          // 정답 입력폼
let $raiseHandBtn = $("#raise-hand");           // 손들기 버튼

let visualTimer = null;       // 화면 타이머 ID
let systemMsgTimer = null;    // 시스템 메시지 자동 숨김 타이머

/**
 * 시스템 메시지 표시/자동 숨김(기본 3초)
 * variant: 'warning'(기본) | 'success' | 'info'
 */
function showSystemMessage(text, variant = 'warning', duration = 3000) {
    const $el = $(".system_message");

    // 이전 타이머 정리
    if (systemMsgTimer) {
        clearTimeout(systemMsgTimer);
        systemMsgTimer = null;
    }

    // 변형 클래스 초기화/적용
    $el.removeClass('success info');
    if (variant === 'success') $el.addClass('success');
    else if (variant === 'info') $el.addClass('info');

    $el.text(text).stop(true, true).fadeIn(150);

    // duration 후 자동 숨김
    systemMsgTimer = setTimeout(() => {
        $el.fadeOut(200);
        systemMsgTimer = null;
    }, duration);
}

function hideSystemMessage() {
    const $el = $(".system_message");
    if (systemMsgTimer) {
        clearTimeout(systemMsgTimer);
        systemMsgTimer = null;
    }
    $el.stop(true, true).fadeOut(120);
}

/**
 * 웹소켓 연결 및 구독
 */
function connectWebSocket() {
    console.log("ws: ", wsUrl);
    const socket = new SockJS('http://localhost:8081/ws?token=' + ACCESS_TOKEN);
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        /**
         * 참여자 입장
         */
        stompClient.subscribe(`/topic/participants/${currentRoomNo}`, function (result) {
            const data = JSON.parse(result.body);
            updateParticipantList(data.list);
            updateBtnByRoomStatus(data);
        });

        /**
         * 게임 시작 버튼
         */
        stompClient.subscribe(`/topic/gameStart/${currentRoomNo}`, function (result) {
            const data = JSON.parse(result.body);
            appendBoardLine(data);
            updateBtnByRoomStatus(data);
        });

        /**
         * 손들기 버튼
         */
        stompClient.subscribe(`/topic/raisehand/${currentRoomNo}`, function (result) {
            const data = JSON.parse(result.body);

            // 타이머 표시
            $("#turn-holder-name").text(data.name);
            $("#turn-info-panel").show();
            startVisualTimer(data.time);

            //msgCnt가 19 이상이면, 정답 벼튼만 활성화, 아니면 그냥 원래대로 활성화
            if (data.msgCnt >= 19) {
                appendBoardLine(data);
                $answerInput.prop("disabled", false);
                $sendAnswerBtn.prop("disabled", false);
                return;
            }
                updateBtnByRoomStatus(data);
        });

        /**
         * 40초 제한 시간 내 입력하지 못한 경우
         */
        stompClient.subscribe(`/topic/turnTimeout/${currentRoomNo}`, function (result) {
            const data = JSON.parse(result.body);
            updateBtnByRoomStatus(data);
        });

        /**
         * 학생 메세지 전송
         */
        stompClient.subscribe(`/topic/sendStdMsg/${currentRoomNo}`, function (result) {
            let data1 = JSON.parse(result.body);
            updateBtnByRoomStatus(data1);
            appendBoardLine(data1);
            stopVisualTimer();
        });

        /**
         * 교사 서버 끊겼을 대
         */
        stompClient.subscribe(`/topic/TeacherDisconnect`, function () {
            window.location.href = '/group';
        });

        /**
         * 교사가 O,X 버튼 눌렀을 때 응답
         */
        stompClient.subscribe(`/topic/TeacherResponce`, function (result) {
            const data = JSON.parse(result.body);

            if (data.system != null) { // 게임이 끝난 경우
                appendBoardLine(data);
                $teacherEndBtn.prop('disabled', false);
                toggleGlow($teacherEndBtn, true);

                $teacherObtn.prop('disabled', true);
                toggleGlow($teacherObtn, false);

                $teacherXbtn.prop('disabled', true);
                toggleGlow($teacherXbtn, false);
                if (Array.isArray(data.msgList)) renderMessageList(data.msgList);
                return;
            }
            // 게임이 끝나지 않고 계속 재개해야하는 경우
            updateBtnByRoomStatus(data);
            if (Array.isArray(data.msgList)) renderMessageList(data.msgList);
        });

        /**
         * 입장 할 때 마다, 사용자 정보를 보냄.
         */
        stompClient.send(`/app/join/${currentRoomNo}`, {}, JSON.stringify({}));
    });
}

/**
 * ajax로 참여자 리스트 가져오기
 */
function getMsgListByRoomNo() {
    $.getJSON(`/api/twenty/getMsgList/` + currentRoomNo, function (msgListData){
        let msgList = msgListData.data;
        console.log(msgList);
        renderMessageList(msgList);
    });
}
/**
 * html 요소가 모두 로딩되면, 아래 기능들이 로딩됨.
 */
$(function () {
    connectWebSocket();
    getMsgListByRoomNo()


    // --- 교사 기능 ---
    /**
     * 교사가 O 버튼
     */
    $teacherObtn.on('click', function (e) {
        e.preventDefault();
        stompClient.send(`/app/teacherResponse`, {}, JSON.stringify({roomNo: currentRoomNo, response: "Y"}));
    });

    /**
     * 교사 X 버튼
     */
    $teacherXbtn.on('click', function (e) {
        e.preventDefault();
        stompClient.send(`/app/teacherResponse`, {}, JSON.stringify({roomNo: currentRoomNo, response: "N"}));
    });

    /**
     * 교사 시작 버튼
     */
    $teacherStartBtn.on('click', function () {
        stompClient.send(`/app/gameStart/${currentRoomNo}`, {}, JSON.stringify({}));
    });

    /**
     * 교사가 게임 종료
     */
    $teacherEndBtn.on('click', function () {
        const sendData = { roomNo: currentRoomNo };
        $.ajax({
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
     * 손들기 버튼
     */
    $raiseHandBtn.on('click', function (e) {
        e.preventDefault();
        stompClient.send(`/app/raisehand/${currentRoomNo}`, {}, JSON.stringify({}));
    });

    /**
     * 질문 전송 버튼
     */
    $sendQuestionBtn.on('click', function (e) {
        e.preventDefault();
        const content = ($questionInput.val() || '').trim();
        if (!content) return;

        const msgData = { userNo: currentUserNo, roomNo: currentRoomNo, type: "Q", content };
        stompClient.send("/app/sendStdMsg", {}, JSON.stringify(msgData));
        $questionInput.val('');
        hideSystemMessage(); // 입력 시 안내 배너 숨김(선택)
    });

    /**
     * 정답 전송 버튼
     */
    $sendAnswerBtn.on('click', function (e) {
        e.preventDefault();
        const content = ($answerInput.val() || '').trim();
        if (!content) return;

        const msgData = { userNo: currentUserNo, roomNo: currentRoomNo, type: "A", content };
        stompClient.send("/app/sendStdMsg", {}, JSON.stringify(msgData));
        $answerInput.val('');
        hideSystemMessage();
    });

    /**
     * Enter 키로 전송 하는 이벤트
     */
    $questionInput.on('keydown', function(e){
        if (e.key === 'Enter' && !$sendQuestionBtn.prop('disabled')) {
            $sendQuestionBtn.click();
        }
    });
    $answerInput.on('keydown', function(e){
        if (e.key === 'Enter' && !$sendAnswerBtn.prop('disabled')) {
            $sendAnswerBtn.click();
        }
    });
});

/**
 * 참여자 리스트 렌더링
 */
function updateParticipantList(userList) {
    let $list = $("#participants-list");
    $list.empty();

    for (let i of userList) {
        const joined = i.status == "JOINED";
        const statusClass = joined ? 'participants_user-join' : 'participants_user';
        const injectHtml = `<div class="${statusClass}" data-user-no="${i.userNo}">${i.nickName}</div>`;
        $list.append(injectHtml);
    }
}


/**
 * 채팅 라인 추가 (시스템/일반)
 */
function appendBoardLine(data) {
    const $board = $("#board-area");

    // 시스템 메시지
    if (data.system != null) {
        showSystemMessage(data.system, data.variant || 'warning'); // 상단 배너, 자동 사라짐
        return;
    }

    // 일반 메시지
    let lineClass = '';
    let senderHtml = '';
    let bubbleClass = '';

    if (data.userNo == currentUserNo) {
        lineClass = 'my-message';
    } else {
        lineClass = 'other-message';
        senderHtml = `<div class="sender">${data.nickName || ''}</div>`;
    }

    switch ((data.type || '').toUpperCase()) {
        case 'Q': bubbleClass = 'question-bubble'; break;
        case 'A': bubbleClass = 'answer-bubble'; break;
        default:  bubbleClass = 'normal-bubble'; break;
    }


    const contentHtml = data.content ? data.content.replace(/\n/g, '<br>') : '';

    const newLineHtml = `
        <div class="message-line ${lineClass}" data-user-no="${data.userNo}" data-log-no="${data.logNo}">
            ${senderHtml}
            <div class="message-bubble ${bubbleClass}">
                ${contentHtml}
            </div>
        </div>
    `;

    $board.append(newLineHtml);
    $board.scrollTop($board[0].scrollHeight);
}

/**
 * 방 상태에 따른 버튼/입력 제어
 */
function updateBtnByRoomStatus(data) {
    const roomStatus = data.roomStatus;
    const winnerNo = data.userNo != null ? data.userNo : null;

    // 초기화
    [
        $teacherObtn, $teacherXbtn, $teacherEndBtn, $teacherStartBtn,
        $sendQuestionBtn, $sendAnswerBtn, $raiseHandBtn
    ].forEach(btn => {
        btn.prop('disabled', true);
        toggleGlow(btn, false);
    });
    $questionInput.prop('disabled', true);
    $answerInput.prop('disabled', true);

    // 상태별
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
 * 버튼 glow 토글
 */
function toggleGlow($el, enabled) {
    if (enabled) $el.addClass('enabled-glow');
    else $el.removeClass('enabled-glow');
}

/**
 * 시각 타이머
 */
function startVisualTimer(time) {
    if (visualTimer) clearInterval(visualTimer);
    let remainingTime = time;
    $('#timer-display').text(remainingTime);

    visualTimer = setInterval(() => {
        remainingTime--;
        $('#timer-display').text(remainingTime);
        if (remainingTime <= 0) {
            stopVisualTimer();
        }
    }, 1000);
}

function stopVisualTimer() {
    if (visualTimer) clearInterval(visualTimer);
    $('#turn-info-panel').hide();
}

/**
 * 메시지 리스트 렌더
 */
function renderMessageList(messageList) {
    const $board = $("#board-area");

    // 시스템 메시지는 유지하고, 나머지 메시지만 삭제
    $board.children(':not(.system_message)').remove();

    if (!Array.isArray(messageList) || messageList.length === 0) {
        return;
    }

    // 최신 리스트 그리기
    messageList.forEach((msg) => {
        appendBoardLine(msg);
    });

    // 스크롤 맨 아래로
    $board.scrollTop($board[0].scrollHeight);
}



