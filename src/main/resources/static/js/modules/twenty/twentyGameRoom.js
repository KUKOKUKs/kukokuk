/**
 * 게임방에 대한 UI 설정이 같이 들어 있는 js 코드
 */

const pageContainer = $(".page_container");
const currentUserNo = pageContainer.data("user-no");
const StringroomNo = pageContainer.data("room-no");
const currentRoomNo = parseInt(StringroomNo);

let stompClient = null;
let turnTimer = null;

// 교사 버튼
let $btnO = $('#btn-o');
let $btnX = $('#btn-x');
let $btnEnd = $("#btn-end");
let $btnStart = $("#btn-start");

// 학생 버튼
let $sendQuestionBtn = $("#send-question");
let $sendAnswerBtn = $("#send-answer");
let $raiseHandBtn = $("#raise-hand");
let $questionInput = $("#question-input");
let $answerInput = $("#answer-input");

let roomStatus = null;

// WebSocket 연결
function connectWebSocket() {
  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    // 구독 코드
    // - 참여자 명단 실시간 반영 - 입장 또는 나감 처리
    stompClient.subscribe(`/topic/participants/${currentRoomNo}`,
        function (result) {
          const data = JSON.parse(result.body);
          updateParticipantList(data.List);
          updateBtnByRoomStatus(data);
        });

    // - 교사가 게임 시작 버튼을 눌렀을 때 UI 변화
    stompClient.subscribe(`/topic/gameStart/${currentRoomNo}`,
        function (result) {
          const data = JSON.parse(result.body);
          console.log("data: ",data);
          appendBoardLine(data);
          updateBtnByRoomStatus(data)
        });

    // 손들기 버튼을 눌렀을 경우 UI 변화
    stompClient.subscribe(`/topic/raisehand/${currentRoomNo}`, function (result) {
      let data = JSON.parse(result.body);
      updateBtnByRoomStatus(data)
        });

    // - 학생이 질문 or 정답 + 교사 OX 버튼 눌렀을 때 실시간 채팅 브로드 캐스팅
    stompClient.subscribe(`/topic/turnOff/${currentRoomNo}`, function (result) {
      resetToDefault();
    });

    // 교사가 게임 종료 버튼 클릭 or 서버 팅김 or 웹 브라우저 탭 닫기 시, 학생들은 그룹 페이지로 이동
    stompClient.subscribe(`/topic/TeacherDisconnect/${currentRoomNo}`, function (result) {
      const data = JSON.parse(result.body);
      let userList = data.userList;
      updateParticipantList(userList);
      updateBtnByRoomStatus(data);
      window.location.href ='/group';
        })

    // 입장할 때마다, 누가 들어왔는지 신호 보내기
    stompClient.send(`/app/join/${currentRoomNo}`, {}, JSON.stringify({}));
  });
}

$(function() {
  connectWebSocket();
  // --- 교사 기능 ---
  // o 버튼 누를 때
  $btnO.click(function () {
    stompClient.send(`/app/chatSend/${currentRoomNo}`, {}, JSON.stringify({}));
    // 아직 어떤 값을 보낼지 정하지 않았음
  });
  // x 버튼 누를 때
  $btnX.click(function () {
    stompClient.send(`/app/chatSend/${currentRoomNo}`, {}, JSON.stringify({}));
  });
  //게임 시작 버튼 눌렀을 때, 게임방 상태 변경
  $btnStart.click(function () {
    stompClient.send(`/app/gameStart/${currentRoomNo}`, {}, JSON.stringify({}));
  });

  //게임 종료 버튼을 눌렀을 때
  $btnEnd.click(function () {
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
    window.location.href ='/group';
  });


  // --- 학생 기능 ---
  // 손들기 버튼을 눌렀을 때, 실시간 신호를 보냄.
  $raiseHandBtn.click(function () {
    stompClient.send(`/app/raisehand/${currentRoomNo}`, {},
        JSON.stringify({}));
  });

  $sendQuestionBtn.click(function () {

  });
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
    stompClient.send(`/app/turnOff/${currentRoomNo}`, {}, JSON.stringify({}));
  }
});


// --- UI 상태 변경 함수들 ---
function activateQuestionMode() {
  clearTimeout(turnTimer);
  document.querySelectorAll(
      '#question-input, #answer-input, #send-question, #send-answer').forEach(
      el => el.disabled = false);
  document.getElementById('raise-hand').disabled = true;
  document.getElementById('question-input').focus();

  turnTimer = setTimeout(() => {
    appendBoardLine({sender: 'system', content: '시간이 초과되었습니다. 턴이 종료됩니다.'});
    stompClient.send(`/app/turnOff/${currentRoomNo}`, {}, JSON.stringify({}));
  }, 60000);
}

function deactivateAllInputs() {
  document.querySelectorAll(
      '#question-input, #answer-input, #send-question, #send-answer, #raise-hand').forEach(
      el => el.disabled = true);
}

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
 * 게임방에 참여자가 입장할 때, 명단 UI 변화
 * @param participants
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
          ${i.name}
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

  if(message.system != null) {
    $firstMessage.text(message.system);
    return;
  }

  if(message.userNo == currentUserNo) {
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
 * 교사가 게임 시작 버튼을 눌렀을 때 UI 변화 메소드
 */
function clickGameStart(status) {
  if(status == 'IN_PROGRESS') {
    $("#btn-o").prop('disabled', false);
    $("#btn-x").prop('disabled', false);
    $("#btn-end").prop('disabled', false);
    $("#btn-start").prop('disabled', true);
    $("#raise-hand").prop('disabled', false);
  }
}

/**
 * 게임방의 상태에 따라, 교사 or 학생 버튼 변화
 */
function updateBtnByRoomStatus(data) {
  let roomStatus = data.roomStatus;
  let winnerNo = data.userNo != null ? data.userNo : null;

  $btnO.prop('disabled', true);
  $btnX.prop('disabled', true);
  $btnEnd.prop('disabled', true);
  $btnStart.prop('disabled', true);
  $sendQuestionBtn.prop('disabled', true);
  $sendAnswerBtn.prop('disabled', true);
  $raiseHandBtn.prop('disabled', true);
  $questionInput.prop('disabled', true);
  $answerInput.prop('disabled', true);

  switch (roomStatus) {
    case 'WAITING' :  // 대기 중
      $btnStart.prop('disabled', false);
      break;
    case 'IN_PROGRESS': // 게임 시작
      $btnEnd.prop('disabled', false);
      $raiseHandBtn.prop('disabled', false);
      break;
    case 'AWAITING_INPUT' : // 학생이 질문 답변 중일 때 상황
        if(currentUserNo == winnerNo) {
          $sendQuestionBtn.prop('disabled', false);
          $sendAnswerBtn.prop('disabled', false);
          $questionInput.prop('disabled', false);
          $answerInput.prop('disabled', false);
        }
      break;
    case 'AWAITING_RESPONSE': // 교사가 답변 중일 때
      $btnO.prop('disabled', false);
      $btnX.prop('disabled', false);
      break;
    default:
      break;
  }
}