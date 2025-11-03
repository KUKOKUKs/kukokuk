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
let $teacherStartBtn = $("#teacher-start-btn");

// 학생 버튼
let $sendQuestionBtn = $("#send-question");     // 질문 전송 버튼
let $questionInput = $("#question-input");      // 질문 입력폼
let $sendAnswerBtn = $("#send-answer");         // 정답 전송 버튼
let $answerInput = $("#answer-input");          // 정답 입력폼
let $raiseHandBtn = $("#raise-hand");           // 손들기 버튼

let $remainingQuestions = $("#remaining-questions");  // 남은 질문횟수
let $commonEndBtn = $("#common-end-btn");             // 종료 버튼
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

        /** 참여자 입장 시, UI 변화
         * 1. stompClient.send(`/app/join/${currentRoomNo}`, {}, JSON.stringify({})); 처음 이 메소드가 동작하여 실시간 서버로 요청을 보냄..
         *
         * 2. 실시간 서버에서는 @MessageMapping으로 받음. -> 참여자 입장 시 필요한 데이터를 조회 및 가공.
         *     -> map 객체에 list,roomStatus를 이름으로 데이터를 할당. -> `/topic/participants/${currentRoomNo}` 이 주소로 map 객체를 브로드캐스팅.
         *                                                           (쉽게 말해, 서버로 부터 데이터를 전달 받는 것이라고 보면 된다.)
         *
         * 3. list : 참여자 리스트를 의미. / 리스트의 각 객체마다. userNo, nickName, status가 담겨 있음.
         *           ++ status는 사용자의 상태를 의미./ LEFT 인지 JOIN 인지./ 방 상태가 아님
         *
         * 4. roomStatus : 방의 상태를 의미. / 이 상태값을 가지고 버튼의 활성화 여부를 설정함.
         *          ++ roomStatus = (COMPLETED, STOPPED..)
         *
         * 5.updateParticipantList(userList) : - 참여자 리스트를 전달 받아, 이 리스트를 화면에 띄워주는 메소드
         *                                     - 참여자의 상태에 따라, 들어왔을 땐 초록색, 나갔을 땐 회색으로 표시
         *
         * 6.updateBtnByRoomStatus(data) : - data를 전달 받아, 메소드 내에서 data.roomStatus를 꺼내,
         *                                 - 방의 상태에 따라 버튼의 활성화 여부(disabled 처리)를 설정함.
         */
        stompClient.subscribe(`/topic/participants/${currentRoomNo}`, function (result) {
            const data = JSON.parse(result.body);       // 서버에서 전달 받은 값을 꺼내고
            updateParticipantList(data.list);           // list의
            updateBtnByRoomStatus(data);
        });

        /** 게임 시작 버튼 눌렀을 때, UI 변화
         * 1.     $teacherStartBtn.on('click', function () {
         *         stompClient.send(`/app/gameStart/${currentRoomNo}`, {}, JSON.stringify({}));
         *     }); 교사가 게임 시작 버튼을 누르면 다음과 같이 실시간 서버에 요청을 보냄.
         *
         * 2. 똑같이 실시간 서버에서 @MessageMapping -> 로직 수행 및 `/topic/gameStart/${currentRoomNo}` 이 주소로 보낼 데이터 가공. ->
         *    `/topic/gameStart/${currentRoomNo}` 브로드 캐스팅
         *
         * 3. 실시간 서버로 부터 받는 데이터 : roomStatus(방 생태), system(시스템 메세지)
         *    - system : 게임 시작 버튼을 누르면 시스템 메시지 처럼 상단에 "스무고개가 시작됩니다." 처럼 띄우기 위한 메세지.
         *
         * 4. appendBoardLine(data) : - 실시간 서버로 부터 내려 받은 데이터(data)를 전달 받아,
         *                              $("#board-area") 이 영역에 실시간 메세지를 띄워주는 메소드임.
         *                            - system 메세지를 전달 받을 경우, 시스템 메세지만 띄우고 종료.
         *                            - 그게 아닌 경우, 나의 메세지는 오른쪽에 보이게 하고 , 다른 사람 메세지는 왼쪽에 보이게
         *                              메세지 리스트를 화면에 띄워줌(카톡 처럼 화면에 메세지들이 화면에 보이는 것이라고 보면됨.)
         *
         * 5. updateBtnByRoomStatus(data) : - data를 전달 받아, 메소드 내에서 data.roomStatus를 꺼내,
         *                                  - 방의 상태에 따라 버튼의 활성화 여부(disabled 처리)를 설정함.
         */
        stompClient.subscribe(`/topic/gameStart/${currentRoomNo}`, function (result) {
            const data = JSON.parse(result.body);
            appendBoardLine(data);
            updateBtnByRoomStatus(data);
        });

        /** 손들기 버튼을 눌렀을 때, UI 변화.
         *  1. $raiseHandBtn.on('click', function (e) {
         *         e.preventDefault();
         *         stompClient.send(`/app/raisehand/${currentRoomNo}`, {}, JSON.stringify({}));
         *     });
         *     - 이 코드를 통해 실시간 서버에 요청 -> 로직 수행 -> 데이터 가공 -> 아래 주소로 브로드캐스팅
         *
         *  2. 실시간 서버로부터 전달 받는 데이터 : userNo,nickName,time,roomStatus, / msgCnt, system
         *        - userNo : 사용자 식별자
         *        - nickName : 사용자 닉네임.
         *        - time : 40초 제한 시간
         *          ++ 타이머 기능은 먼저 서버에서 40초 제한 시간을 부여 하도록 했음. - 그래야 모든 사용자에게 40초 제한 시간이 걸림.
         *            따라서 프론트에서는 40,39,38.. 숫자가 세지는 UI 변화만 일으키면 되기 때문에 40 이라는 time 값을 전달한 것임.
         *        - roomStatus : 방 상태
         *
         *        - msgCnt : 이 방의 총 메세지 개수. / 이는 손들기 버튼을 눌렀을 때,
         *                                         이 방의 메세지 개수가 19개 이상일 경우 "정답을 입력하세요" 라는 시스템 메세지를 띄우기 위한 것.
         *                                         또한 질문 입력창은 입력할 수 없고, 정답 입력창만 입력하도록 한 것임.
         *        - system : msgCnt와 같이 서버 단에서 이 방의 총 메세지 개수가 19개 이상일 경우 system 메세지를 저장 시킴
         *                   메세지 내용은 "정답을 입력해주세요." 라고 되어 있음.
         *
         *        ++ 정리하면 msgCnt와 system은 서버 단에서 이 게임방의 메세지가 19개 이상일 경우에만
         *           msgCnt의 값과 system 메세지 값을 map 객체에 담아 여기로 전달 되게 했음.
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

        /** 40초 제한 시간 내 입력하지 못한 경우
         *  1. 위 손들기 버튼에서 40초 제한 시간이 지나게 되면 DB 작업은 실시간 서버 단에서 로직 수행함.
         *  2. 여기서는 roomStatus만 전달함.
         */
        stompClient.subscribe(`/topic/turnTimeout/${currentRoomNo}`, function (result) {
            const data = JSON.parse(result.body);
            updateBtnByRoomStatus(data);
        });

        /** 학생이 메세지 전송 시, UI 변화
         * $sendQuestionBtn.on('click', function (e) {...}
         * $sendAnswerBtn.on('click', function (e) {...}
         * 이 부분에서 사용자가 입력한 메세지를 실시간 서버로 요청 -> 로직 수행 -> 브로드캐스팅
         * 1. 서버에서 내려 받는 값 :
         *    - logNo : kukokuk_twenty_logs 테이블의 식별값
         *    - userNo : 사용자 식별자
         *    - msgType : 이 메세지가 정답(A) 인지, 질문(Q) 인지
         *    - content : 메세지 내용.
         *    - nickName : 사용자 닉네임
         *    - roomStatus : 방의 상태
         * 2.appendBoardLine(data)
         *  - system 메세지가 있을 경우, system 메세지만 띄우고 종료.
         *  - system 메세지가 없을 경우, 로직
         *      - 먼저 서버에서 내려받은 userNo와 전역변수로 설정된 currentUserNo와 비교하여
         *        같을 경우 = 내가 보낸 메세지 이므로, 오른쪽에 표시되게 함.
         *        아닌 경우 = 왼쪽에 표시되게 함.
         *      - 질문의 타입에 따라(Q or A) 인지에 따라, 적용하는 스타일을 다르게 함.
         *      - content 값으로 메세지 내용을 화면에 표시
         *      - logNo와 userNo는 각 메세지의 data 속성으로 일단 넣어둔 것임.
         */
        stompClient.subscribe(`/topic/sendStdMsg/${currentRoomNo}`, function (result) {
            let data1 = JSON.parse(result.body);
            let msgCnt = data1.msgCnt;
            $remainingQuestions.text(20 - msgCnt);
            updateBtnByRoomStatus(data1);    // 버튼 변화
            appendBoardLine(data1);          // 메세지 UI 표현
            stopVisualTimer(); // 타이머 제거 메소드
        });

        /** 교사 서버 끊겼을 때, 알림창이 나오고 그룹 페이지로 이동.
         * 1. 교사가 서버가 끊기거나, 웹브라우저 탭을 닫을 경우 (게임 종료 버튼 누를 때 x)
         * 2. 경고창을 띄우고, 그룹 페이지로 이동함.
         */
        stompClient.subscribe(`/topic/TeacherDisconnect`, function () {
            const data = {
                system : "스무고개가 중단되었습니다. 잠시 후에 이동합니다..."
            };
            appendBoardLine(data)
            // 5초(5000ms) 대기 후 이동
            setTimeout(() => {
                window.location.href = '/group';
            }, 5000);
        });

        /** 교사가 O,X 버튼 눌렀을 때 응답
         * $teacherObtn.on('click', function (e) {..}
         * $teacherXbtn.on('click', function (e) {..}
         * - 위 코드로 실시간 서버에 학생의 메세지에 O,X에 대한 요청을 실시간 서버에 보냄. -> 로직 수행 -> 브로드 캐스팅
         * - 서버에서 내려받는 데이터
         *  - var : 스무고개 종료되었다는 신호임.
         *  - roomStatus : 방의 상태
         *  - msgList : 전체 메세지 리스트 -> 교사가 O,X 버튼을 누를 때마다 전체 메세지 리스트를 실시간으로 갱신하기 위함.
         *    => 왜냐하면, 학생A: "사람인가요?" -> 학생A : "사람인가요? :⭕" 이런 식으로 DB에 다시 저장해서 최신 리스트를 갱신해서
         *       화면에 뿌려줘야하기 때문.
         *
         * ++ function toggleGlow($el, enabled) {} : 얘는 그냥 스타일 효과만 샤라랄 하고 내주는 역할임.
         */
        stompClient.subscribe(`/topic/TeacherResponce`, function (result) {
            const data = JSON.parse(result.body);

            if (data.var != null) { // 게임이 끝난 경우
                let message;
                // 스무고개 승리 여부에 따른 안내 메세지를 구성해서 띄워주고,
                if(data.teacherResponse == "Y" ) {
                    message = {
                        system : "승리하셨습니다! 종료를 눌러주세요"
                    }
                } else {
                    message = {
                        system : "패배 하셨습니다.. 종료를 눌러주세요"
                    }
                }
                appendBoardLine(message);  // 이게 안내 메세지 띄워주는 메소드

                $teacherObtn.prop('disabled', true);   //O,X 버튼 비활성화 해주고
                toggleGlow($teacherObtn, false);

                $teacherXbtn.prop('disabled', true);
                toggleGlow($teacherXbtn, false);
                if (Array.isArray(data.msgList)) renderMessageList(data.msgList);  // 이 게임방의 전체 메세지 리스트를 다시 갱신 해준다.
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

    // -- 교사&학생 공통 기능 : 종료 버튼에 대한 처리
    /** 종료 버튼 일괄 처리
     * roomNo를 비동기로 보내서, 현재 게임방의 상태를 반환.
     * - 현재 게임방의 상태에 따라
     *
     */
    $commonEndBtn.on('click', function () {
        const sendData = { roomNo: currentRoomNo };
        $.ajax({
            url: '/api/twenty/endBtn',
            type: 'POST',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(sendData),
            success: function (data) {
                let currentRoom = data.data;
                console.log(currentRoom);
                // 정상 종료 상태에서 버튼을 누른거면 그룹 페이지로 이동.
                if(currentRoom.status == "COMPLETED"){
                    window.location.href = `/twenty/result/${currentRoomNo}`;
                } else {
                    window.location.href = '/group';     //그게 아니라면 그룹페이지로 이동.
                }
            }
        });
    });


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
 * userList에서 각각 userNo, nickName, status(유저의 상태가 들어있음. LEFT 인지 JOIN 인지)
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
        $teacherObtn, $teacherXbtn, $teacherStartBtn,
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



