/**
 * 게임방에 대한 UI 설정이 같이 들어 있는 js 코드
 */

const pageContainer = $(".page_container");

const currentUserNo = pageContainer.data("user-no");
const StringroomNo =  pageContainer.data("room-no");
const currentRoomNo = parseInt(StringroomNo);

let stompClient = null;
let turnTimer = null;

// WebSocket 연결
function connectWebSocket() {
  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    // 구독 코드
    // 1. 채팅 영역 실시간 반영
    stompClient.subscribe(`/topic/twentyRoom/${currentRoomNo}`, function(result) {
      const message = JSON.parse(result.body);
      appendBoardLine(message);
    });

    // 2.참여자 명단 실시간 반영
    stompClient.subscribe(`/topic/participants/${currentRoomNo}`, function(result) {
      const userList = JSON.parse(result.body);
      console.log("userList: ",userList);
      updateParticipantList(userList);
    });

    stompClient.subscribe(`/topic/turnOff/${currentRoomNo}`, function(result) {
      resetToDefault();
    });

    // 손든 사람!!
    stompClient.subscribe(`/topic/raisehand/${currentRoomNo}`, function(result) {
      const winner = JSON.parse(result.body);
      if (winner.userNo == currentUserNo) {
        activateQuestionMode();
      } else {
        deactivateAllInputs();
      }
    });

    // 입장할 때마다, 누가 들어왔는지 신호 보내기
    stompClient.send(`/app/join/${currentRoomNo}`, {}, JSON.stringify({}));
  });
}

// 이벤트 리스너: DOM이 로드된 후 실행되도록 변경 (기존의 $(function(){...})와 동일한 역할)
document.addEventListener('DOMContentLoaded', function() {
  connectWebSocket();

  // --- 교사 기능 ---
  const btnO = document.getElementById('btn-o');
  const btnX = document.getElementById('btn-x');
  const btnEnd = document.getElementById('btn-end');

  if (btnO) {
    btnO.addEventListener('click', handleOXClick);
  }
  if (btnX) {
    btnX.addEventListener('click', handleOXClick);
  }
  if (btnEnd) {
    btnEnd.addEventListener('click', function() {
      stompClient.send(`/app/gameOver/${currentRoomNo}`, {}, JSON.stringify({}));
    });
  }
  // 눌렀을 때 웹 소캣 신호 보내는거
  function handleOXClick(event) {
    const msg = {
      content: event.target.textContent
    };
    stompClient.send(`/app/chatSend/${currentRoomNo}`, {}, JSON.stringify(msg));
  }


  // --- 학생 기능 ---
  const sendQuestionBtn = document.getElementById('send-question');
  const sendAnswerBtn = document.getElementById('send-answer');
  const raiseHandBtn = document.getElementById('raise-hand');

  if (sendQuestionBtn) {
    sendQuestionBtn.addEventListener('click', handleSendClick);
  }
  if (sendAnswerBtn) {
    sendAnswerBtn.addEventListener('click', handleSendClick);
  }
  if (raiseHandBtn) {
    raiseHandBtn.addEventListener('click', function() {
      stompClient.send(`/app/raisehand/${currentRoomNo}`, {}, JSON.stringify({}));
    });
  }

  function handleSendClick(event) {
    const isQuestion = event.target.id === 'send-question';
    const input = document.getElementById(isQuestion ? 'question-input' : 'answer-input');
    const text = input.value.trim();

    if (text) {
      const msg = {
        roomId: currentRoomNo,
        content: text
      };
      stompClient.send(`/app/chatSend/${currentRoomNo}`, {}, JSON.stringify(msg));
      input.value = '';
    }
    stompClient.send(`/app/turnOff/${currentRoomNo}`, {}, JSON.stringify({}));
  }
});


// --- UI 상태 변경 함수들 ---
function activateQuestionMode() {
  clearTimeout(turnTimer);
  document.querySelectorAll('#question-input, #answer-input, #send-question, #send-answer').forEach(el => el.disabled = false);
  document.getElementById('raise-hand').disabled = true;
  document.getElementById('question-input').focus();

  turnTimer = setTimeout(() => {
    appendBoardLine({ sender: 'system', content: '시간이 초과되었습니다. 턴이 종료됩니다.' });
    stompClient.send(`/app/turnOff/${currentRoomNo}`, {}, JSON.stringify({}));
  }, 60000);
}

function deactivateAllInputs() {
  document.querySelectorAll('#question-input, #answer-input, #send-question, #send-answer, #raise-hand').forEach(el => el.disabled = true);
}

function resetToDefault() {
  clearTimeout(turnTimer);
  document.querySelectorAll('#question-input, #answer-input, #send-question, #send-answer').forEach(el => {
    el.disabled = true;
    if (el.tagName === 'INPUT') el.value = '';
  });
  document.getElementById('raise-hand').disabled = false;
}

/**
 * 게임방의 참여자가 입장 시, UI 변화
 * @param participants
 */
function updateParticipantList(userList) {
  console.log("userList2: ",userList);
  let $list = $("#participants-list");
  $list.empty();

  for (let i of userList) {
    // 1. 오타 수정: stausClass -> statusClass
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

function appendBoardLine(message) {
  const board = document.getElementById('board-area');
  let lineClass = '';
  let senderHtml = '';

  const firstMessage = board.querySelector('.system-message');
  if (firstMessage && board.children.length === 1) {
    board.innerHTML = '';
  }

  if (message.sender === 'system') {
    lineClass = 'system-message';
  } else if (message.sender === currentUser.username) {
    lineClass = 'my-message';
  } else {
    lineClass = 'other-message';
    senderHtml = `<div class="sender">${message.sender}</div>`;
  }

  const contentHtml = message.content.replace(/\n/g, '<br>');
  const newLineHtml = `<div class="message-line ${lineClass}">${senderHtml}<div>${contentHtml}</div></div>`;

  board.insertAdjacentHTML('beforeend', newLineHtml);
  board.scrollTop = board.scrollHeight;
}