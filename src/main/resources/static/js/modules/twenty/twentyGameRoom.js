/**
 * 게임방에 대한 UI 설정이 같이 들어 있는 js 코드
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
let $sendQuestionBtn = $("#send-question");
let $sendAnswerBtn = $("#send-answer");
let $raiseHandBtn = $("#raise-hand");
let $questionInput = $("#question-input");
let $answerInput = $("#answer-input");

let visualTimer = null; // 화면 타이머 ID를 저장할 전역 변수

// WebSocket 연결
function connectWebSocket() {
    console.log("ws: ", wsUrl);
    const socket = new SockJS('http://localhost:8081/ws?token=' +ACCESS_TOKEN);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        // 구독 코드
        // - 참여자 명단 실시간 반영 - 입장 또는 나감 처리
        stompClient.subscribe(`/topic/participants/${currentRoomNo}`,
            function (result) {
                const data = JSON.parse(result.body);
                //data => list, roomStatus
                updateParticipantList(data.list);
                updateBtnByRoomStatus(data);
            });

        // - 교사가 게임 시작 버튼을 눌렀을 때 UI 변화
        stompClient.subscribe(`/topic/gameStart/${currentRoomNo}`,
            function (result) {
                const data = JSON.parse(result.body);
                console.log("data: ", data);
                appendBoardLine(data);
                updateBtnByRoomStatus(data)
            });

        // 손들기 버튼을 눌렀을 경우 UI 변화
        stompClient.subscribe(`/topic/raisehand/${currentRoomNo}`,
            function (result) {
                let data = JSON.parse(result.body);
                $("#turn-holder-name").text(data.name);
                $("#turn-info-panel").show();
                startVisualTimer(data.time);
                updateBtnByRoomStatus(data)
            });

        // - 학생이 질문 or 정답 + 교사 OX 버튼 눌렀을 때 실시간 채팅 브로드 캐스팅
        stompClient.subscribe(`/topic/turnOff/${currentRoomNo}`,
            function (result) {
                resetToDefault();
            });

        // 교사가 게임 종료 버튼 클릭 or 서버 팅김 or 웹 브라우저 탭 닫기 시, 학생들은 그룹 페이지로 이동
        stompClient.subscribe(`/topic/TeacherDisconnect/${currentRoomNo}`,
            function (result) {
                const data = JSON.parse(result.body);
                let userList = data.userList;
                updateParticipantList(userList);
                updateBtnByRoomStatus(data);
                window.location.href = '/group';
            })

        // 입장할 때마다, 누가 들어왔는지 신호 보내기
        stompClient.send(`/app/join/${currentRoomNo}`, {}, JSON.stringify({}));
    });
}

$(function () {
    connectWebSocket();
    // --- 교사 기능 ---
    // o 버튼 누를 때
    $teacherObtn.click(function () {
        stompClient.send(`/app/chatSend/${currentRoomNo}`, {},
            JSON.stringify({}));
        // 아직 어떤 값을 보낼지 정하지 않았음
    });
    // x 버튼 누를 때
    $teacherXbtn.click(function () {
        stompClient.send(`/app/chatSend/${currentRoomNo}`, {},
            JSON.stringify({}));
    });
    //게임 시작 버튼 눌렀을 때, 게임방 상태 변경
    $teacherStartBtn.click(function () {
        stompClient.send(`/app/gameStart/${currentRoomNo}`, {},
            JSON.stringify({}));
    });

    //게임 종료 버튼을 눌렀을 때
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
    // 손들기 버튼을 눌렀을 때, 실시간 신호를 보냄.
    $raiseHandBtn.click(function () {
        stompClient.send(`/app/raisehand/${currentRoomNo}`, {},
            JSON.stringify({}));
    });

    // 질문을 눌렀을 때
    $sendQuestionBtn.click(function () {

    });

    // 정답을 눌렀을 때
    $sendAnswerBtn.click(function () {

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

function resetToDefault() {
    clearTimeout(turnTimer);
    document.querySelectorAll(
        '#question-input, #answer-input, #send-question, #send-answer').forEach(
        el => {
            el.disabled = true;
            if (el.tagName === 'INPUT') {
                el.value = '';
            }
        });
    document.getElementById('raise-hand').disabled = false;
}

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
function appendBoardLine(message) {
    let $board = $("#board-area");
    let $firstMessage = $(".system_message");

    let lineClass = '';
    let senderHtml = '';

    if (message.system != null) {
        $firstMessage.text(message.system);
        return;
    }

    if (message.userNo == currentUserNo) {
        lineClass = 'my-message';
    } else {
        lineClass = 'other-message';
        senderHtml = `<div class="sender">${message.sender}</div>`;
    }

    const contentHtml = message.message.replace(/\n/g, '<br>');
    const newLineHtml = `<div class="message-line ${lineClass}">${senderHtml}<div>${contentHtml}</div></div>`;

    $board.append(newLineHtml);
    $board.scrollTop = $board.scrollHeight;
}

/**
 * 게임방의 상태에 따라, 교사 or 학생 버튼 변화
 */
function updateBtnByRoomStatus(data) {
    let roomStatus = data.roomStatus;
    let winnerNo = data.userNo != null ? data.userNo : null;

    $teacherObtn.prop('disabled', true);
    $teacherXbtn.prop('disabled', true);
    $teacherEndBtn.prop('disabled', true);
    $teacherStartBtn.prop('disabled', true);
    $sendQuestionBtn.prop('disabled', true);
    $sendAnswerBtn.prop('disabled', true);
    $raiseHandBtn.prop('disabled', true);
    $questionInput.prop('disabled', true);
    $answerInput.prop('disabled', true);

    switch (roomStatus) {
        case 'WAITING' :  // 대기 중
            $teacherStartBtn.prop('disabled', false);
            break;
        case 'IN_PROGRESS': // 게임 시작
            $teacherEndBtn.prop('disabled', false);
            $raiseHandBtn.prop('disabled', false);
            break;
        case 'AWAITING_INPUT' : // 학생이 질문 답변 중일 때 상황

            if (currentUserNo == winnerNo) {
                $sendQuestionBtn.prop('disabled', false);
                $sendAnswerBtn.prop('disabled', false);
                $questionInput.prop('disabled', false);
                $answerInput.prop('disabled', false);
            }
            break;
        case 'AWAITING_RESPONSE': // 교사가 답변 중일 때
            $teacherObtn.prop('disabled', false);
            $teacherXbtn.prop('disabled', false);
            break;
        default:
            break;
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