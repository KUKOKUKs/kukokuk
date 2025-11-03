/**
 * 상수가 아닌 변수를 전역으로 선언해버릴 경우 DOM요소가 선택되지 않을 가능성이 있으며
 * 다른곳에서 같은 변수 선언시 충돌 발생으로 주의해야 함
 * 안전하게 문서 읽기가 완료 된 이후에 작성 권장
 */
$(document).ready(function () {
    // 스무고개 관련
    // 페이지 컨테이너라는 명칭이 맞는 컨텐츠가 아니므로 수정
    // const pageContainer = $(".page_container"); // 제이쿼리 선택자는 관례적으로 앞에 $를 붙여 제이쿼리 선택자라는 것을 명시(체이닝을 할 수 있다)
    // currentUserNo는 그냥 사용하면서 currentRoomNo만 ParseInt로 타입을 변환하는 것임?
    // const StringroomNo = $twentyContainer.data("room-no");
    // const currentRoomNo = parseInt(StringroomNo);

    const $twentyContainer = $(".twenty_container"); // 스무고개 컨테이너 요소
    const currentUserNo = Number($twentyContainer.data("user-no")); // 사용자 번호
    const currentRoomNo = Number($twentyContainer.data("room-no")); // 스무고개 방 번호
    const wsUrl = $twentyContainer.data("ws-url"); // 웹 소켓 경로
    const ACCESS_TOKEN = $twentyContainer.data("access-token"); // 웹 소켓 인증 토큰
    const $sysytemAlarm = $("#system-alarm"); // 시스템 알람 요소
    const $turnInfoPanel = $('#turn-info-panel'); // 타이머 부모 요소
    const $timerDisplay = $('#timer-display'); // 타이머 요소
    const $turnHolderName = $("#turn-holder-name"); // 타이머 사용자 이름 요소
    const $participantsList = $("#participants-list"); // 참여자 리스트 요소
    const $chatsBoard = $("#board-area"); // 채팅 요소

    // 교사 버튼
    const $teacherControls = $("#teacher-controls"); // 교사 OX 버튼 부모 요소
    let $teacherObtn = $('#teacher-o-btn'); // 교사 O 버튼 요소
    let $teacherXbtn = $('#teacher-x-btn'); // 교사 X 버튼 요소
    let $teacherStartBtn = $("#teacher-start-btn"); // 교사 시작 버튼 요소

    // 학생 버튼
    const $waitingButton = $("#waiting-button"); // 대기 버튼(모양만)
    const $studentControls = $("#student-controls"); // 학생 질문/정답 제출 버튼 부모 요소
    const $twentyUserAnswerForm = $("#twenty-user-answer-form"); // 질문/정답 입력 폼
    const $userAnswer = $("#user-answer"); // 질문/정답 입력 인풋 요소
    let $raiseHandBtn = $("#raise-button");           // 손들기 버튼
    let $sendQuestionBtn = $("#send-question");     // 질문 전송 버튼
    let $sendAnswerBtn = $("#send-answer");         // 정답 전송 버튼

    // 하나의 인풋으로 사용하여 필요없음
    // let $questionInput = $("#question-input");      // 질문 입력폼
    // let $answerInput = $("#answer-input");          // 정답 입력폼

    let $remainingQuestions = $("#remaining-questions");  // 남은 질문횟수
    let $commonEndBtn = $("#common-end-btn");             // 종료 버튼
    let visualTimer = null;       // 화면 타이머 ID
    let systemMsgTimer = null;    // 시스템 메시지 자동 숨김 타이머

    let stompClient = null;
    let turnTimer = null; // 이건 사용되지 않고 있음

    // 교사, 학생 종료 버튼 이벤트 핸들러
    // roomNo로 현재 방 상태에 비동기 요청으로 상태에 따라 페이지 이동
    $commonEndBtn.on('click', function () {
        console.log("종료하기 버튼 실행");

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
                if(currentRoom.status === "COMPLETED"){
                    window.location.href = `/twenty/result/${currentRoomNo}`;
                } else {
                    window.location.href = '/group';     //그게 아니라면 그룹페이지로 이동.
                }
            }
        });
    });

    // 교사의 시작 버튼 이벤트 핸들러
    $teacherStartBtn.on('click', function (e) {
        console.log("스무고개 시작하기 실행");
        e.preventDefault();
        stompClient.send(`/app/gameStart/${currentRoomNo}`, {}, JSON.stringify({}));
        $(this).hide(); // 시작 후 버튼 숨김
        $teacherControls.removeClass("disabled") // OX 버튼 노출
    });

    // 교사의 O 버튼 이벤트 핸들러
    $teacherObtn.on('click', function (e) {
        console.log("교사의 O 버튼 실행");
        e.preventDefault();
        stompClient.send(`/app/teacherResponse`, {}, JSON.stringify({roomNo: currentRoomNo, response: "Y"}));
    });

    // 교사의 X 버튼 이벤트 핸들러
    $teacherXbtn.on('click', function (e) {
        console.log("교사의 X 버튼 실행");
        e.preventDefault();
        stompClient.send(`/app/teacherResponse`, {}, JSON.stringify({roomNo: currentRoomNo, response: "N"}));
    });

    // 학생의 손들기 버튼 이벤트 핸들러
    $raiseHandBtn.on('click', function (e) {
        console.log("손들기 버튼 실행");
        e.preventDefault();
        stompClient.send(`/app/raisehand/${currentRoomNo}`, {}, JSON.stringify({}));
    });

    // 학생의 질문 전송 버튼 이벤트 핸들러
    $sendQuestionBtn.on('click', function (e) {
        console.log("질문 전송 버튼 실행");
        e.preventDefault();
        const content = ($userAnswer.val() || '').trim();
        if (!content) return;

        const msgData = { userNo: currentUserNo, roomNo: currentRoomNo, type: "Q", content };
        stompClient.send("/app/sendStdMsg", {}, JSON.stringify(msgData));
        $userAnswer.val('');
        hideSystemMessage(); // 입력 시 안내 배너 숨김(선택)
    });

    // 학생의 정답 전송 버튼 이벤트 핸들러
    $sendAnswerBtn.on('click', function (e) {
        console.log("정답 전송 버튼 실행");
        e.preventDefault();
        const content = ($userAnswer.val() || '').trim();
        if (!content) return;

        const msgData = { userNo: currentUserNo, roomNo: currentRoomNo, type: "A", content };
        stompClient.send("/app/sendStdMsg", {}, JSON.stringify(msgData));
        $userAnswer.val('');
        hideSystemMessage();
    });

    // 엔터키로 전송 이벤트 핸들러
    // 이 부분은 폼태그로 감싸서 해당 폼의 submit 이벤트로 일괄 제어 가능함
    // 추가적인 사항으로 버튼을 눌러야 제출임을 알도록 엔터입력 시 제출되지 않도록 함
    // $questionInput.on('keydown', function(e){
    //     console.log("질문 제출 실행");
    //     if (e.key === 'Enter' && !$sendQuestionBtn.prop('disabled')) {
    //         $sendQuestionBtn.click();
    //     }
    // });

    // $answerInput.on('keydown', function(e){
    //     console.log("정답 제출 실행");
    //     if (e.key === 'Enter' && !$sendAnswerBtn.prop('disabled')) {
    //         $sendAnswerBtn.click();
    //     }
    // });

    // 질문/정답 입력 폼 요소 제출 이벤트 핸들러
    // submit 버튼, enter키로 제출 제어 가능
    // 인풋을 감싸는 요소를 form 태그로 하지않으면 제출 자체가 되지 않지만
    // 설명을 위해 form으로 감싸서 제출이벤트 발생 시 알리도록 구현 함
    // 실질적으로는 인풋을 감싸는 요소를 DIV로 하고 아래 핸들러는 없어도 됨
    $twentyUserAnswerForm.submit(function (e) {
        e.preventDefault();
        alert("질문 또는 정답 버튼을 눌러주세요!!!");
        return false;
    });

    // 시스템 메시지 표시/자동 숨김(기본 3초)
    // 정확한 판단을 할 수 있는 명칭들로 사용 text -> systemMsg 수정
    function showSystemMessage(systemMsg, duration = 3000) {
        console.log("showSystemMessage()");
        // const $el = $(".system_message");

        if (!systemMsg) return; // 분기 처리 확실히

        // 이전 타이머 정리
        if (systemMsgTimer) {
            clearTimeout(systemMsgTimer);
            systemMsgTimer = null;
        }

        $sysytemAlarm.text(systemMsg).stop(true, true).fadeIn(150);

        // duration 후 자동 숨김
        systemMsgTimer = setTimeout(() => {
            $sysytemAlarm.fadeOut(200);
            systemMsgTimer = null;
        }, duration);
    }

    // 시스템 알람 숨기기
    function hideSystemMessage() {
        console.log("hideSystemMessage()");
        // const $el = $(".system_message");
        if (systemMsgTimer) {
            clearTimeout(systemMsgTimer);
            systemMsgTimer = null;
        }
        $sysytemAlarm.stop(true, true).fadeOut(120);
    }

    // 타이머 시작 함수
    function startVisualTimer(time) {
        console.log("startVisualTimer()");

        if (visualTimer) clearInterval(visualTimer);
        let remainingTime = time;
        $timerDisplay.text(remainingTime);

        visualTimer = setInterval(() => {
            remainingTime--;
            $timerDisplay.text(remainingTime);
            if (remainingTime <= 0) {
                stopVisualTimer();
            }
        }, 1000);
    }

    // 타이머 중단 함수
    function stopVisualTimer() {
        console.log("stopVisualTimer()");

        if (visualTimer) clearInterval(visualTimer);
        $turnInfoPanel.hide();
    }

    /**
     * 채팅 라인 추가
     * 이 함수는 생성형 함수로 DOM에 관여하지 않도록 생성된 값만 반환 하도록 함
     * 단일 데이터들 리스트 데이터든 일괄적으로 처리할 수 있도록 재사용 효율을 늘림
     * appendBoardLine를 이에 맞는 함수명으로 수정
     * @param chatData 채팅 정보
     * @returns {string} 생성된 채팅 리스트 html 문자열
     */
    function getChatListHtml(chatData) {
        console.log("getChatListHtml()");

        // const $board = $("#board-area"); // 상단부에 미리 선언

        // 시스템 메시지
        // showSystemMessage 함수 내에서 분기 처리로 간편 사용(간편 기능 함수이기 때문)
        // 리펙토링 사항으로 분리 현재 로직내에 있으므로써 재사용에 혼동 및 불편을 줌
        // if (data.system != null) {
        //     showSystemMessage(data.system);
        //     return;
        // }

        // 단일 객체라도 배열 형태로 통일
        // 단일 데이터라도 반복문으로 처리
        // 단일일 경우 한번만 반복되므로 성능상 문제 없음
        const chats = Array.isArray(chatData) ? chatData : [chatData];
        let chatListHtml = '';

        // 일반 메시지
        let lineClass = '';
        let senderHtml = '';

        chats.forEach((chat) => {
            // 내 메시지 / 상대 메시지 구분
            if (chat.userNo === currentUserNo) {
                lineClass = 'my_message';
            } else {
                lineClass = 'other_message';
                senderHtml = `<div class="sender">${chat.nickname || ''}</div>`;
            }

            // 메시지 타입에 따라 스타일 지정
            // switch ((chat.type || '').toUpperCase()) {
            //     case 'Q': bubbleClass = 'question-bubble'; break;
            //     case 'A': bubbleClass = 'answer-bubble'; break;
            //     default:  bubbleClass = 'normal-bubble'; break;
            // }

            const bubbleClass = chat.type === "Q" ? "question" : "answer";

            // <pre> 태그 또는 css로 적용이 가능함 부수적인 자바스크립트 코드 필요 없음
            // const contentHtml = data.content ? data.content.replace(/\n/g, '<br>') : '';

            // 채팅 리스트 추가
            // chatListHtml += `
            //     <div class="message_line ${lineClass}" data-user-no="${chat.userNo}" data-log-no="${chat.logNo}">
            //         ${senderHtml}
            //         <div class="message_bubble ${bubbleClass}">
            //             ${chat.content}
            //         </div>
            //     </div>
            // `;

            chatListHtml += `
                <div class="message_line ${lineClass}" data-user-no="${chat.userNo}" data-log-no="${chat.logNo}">
                    <div class="thumbnail">
                        <img src="${chat.profileFileUrl}" alt="profile thumbnail">
                    </div>
                    <div class="chat_info">
                        <div class="sender">${senderHtml}</div>
                        <div class="message_bubble ${bubbleClass}">${chat.content}</div>
                    </div>
                </div>
            `;
        });

        // 호출부에서 DOM 조작
        // $chatsBoard.append(newLineHtml);
        // $chatsBoard.scrollTop($chatsBoard[0].scrollHeight);

        return chatListHtml;
    }

    // 메시지 리스트 렌더링 함수
    function renderMessageList(messageList) {
        console.log("renderMessageList()");

        // const $board = $("#board-area"); // 이미 상단에 선언해 놓았음

        // 시스템 메시지는 유지하고, 나머지 메시지만 삭제
        // $board.children(':not(.system_message)').remove(); // 담당 구역 완전 분리로 채팅 보드 요소에 포함되지 않음

        if (!Array.isArray(messageList) || messageList.length === 0) {
            return;
        }

        // 최신 리스트 그리기
        // 각 순환 마다 매번 함수 호출은 비효율적임 이 내부 로직에서 이 함수 로직의 중복 코드가 발생하더라도 직접 구현이 성능적으로 우수함
        // messageList.forEach((msg) => {
        //     appendBoardLine(msg);
        // });

        // 미리 함수 내에서 단일 데이터든 리스트 데이터든 일괄 처리 가능하도록 구현해 놓았음
        // DOM 조작은 호출부에서 실행
        const chatListHtml = getChatListHtml(messageList);
        $chatsBoard.append(chatListHtml);

        // 스크롤 맨 아래로
        // 기존 코드에서는 이 수행문이 함수 내에서도 호출한 함수내에도 수행됨
        // forEach로 반복되는 횟수만큼 수행되므로 퍼포먼스에 매우 좋지 않음
        $chatsBoard.scrollTop($chatsBoard[0].scrollHeight);
    }

    /**
     * 참여자 리스트 HTML 문자열 생성
     * userList에서 각각 userNo, nickName, status(유저의 상태가 들어있음. LEFT 인지 JOIN 인지)
     * updateParticipantList를 이에 맞는 함수명으로 수정
     * DOM을 조작하는 부분은 호출부에서 처리 이 함수는 호출부에서 처리를 위한 내용 생성
     * !책임분리 중요!
     * ㄴ 재사용성, 유지보수 간편, 성능 개선 여지, 테스트 용이 ... 등 효과적임
     * @param userList 참여자 리스트 정보
     * @returns {string} DOM에 추가할 HTML 문자열
     */
    function getParticipantListHtml(userList) {
        console.log("getParticipantListHtml()");

        // let $list = $("#participants-list"); // 상단 부에 미리 선언
        // $participantsList.empty();

        let injectHtml = ""; // 참여자 리스트 요소 문자열을 담을 변수
        for (let player of userList) {
            const joined = player.status === "JOINED";
            const statusClass = joined ? '' : 'disabled';

            // 참여자 리스트 요소에 추가할 요소 문자열 누적
            // injectHtml += `<div class="${statusClass}" data-user-no="${user.userNo}">${user.nickName}</div>`;

            injectHtml += `
                <div class="profile_info small ${statusClass}" data-participant-user-no="${player.userNo}">
                    <div class="thumbnail">
                        <img src="${player.profileFileUrl}" alt="profile thumbnail">
                    </div>
                    <p class="name">${player.nickname}</p>
                </div>
            `;

            // $participantsList.append(injectHtml);
        }
        // $participantsList.html(injectHtml); // empty, append로 두번의 메소드 사용없이도 html로 해결 가능(같은 기능을 구사함)

        return injectHtml;
    }

    // 방 상태에 따른 버튼/입력 제어
    function updateBtnByRoomStatus(data) {
        console.log("updateBtnByRoomStatus()");

        const roomStatus = data.roomStatus;
        const winnerNo = data.userNo != null ? data.userNo : null; // 손든사람

        // 초기화
        [
            $teacherObtn, $teacherXbtn, $teacherStartBtn,
            $sendQuestionBtn, $sendAnswerBtn,
            $twentyUserAnswerForm
        ].forEach(disabledElement => {
            disabledElement.addClass('disabled');
        });

        // 교사 관련
        $teacherControls.removeClass("d_none"); // 교사 OX 버튼 부모 요소 노출(WAITING 상태 외 노출)
        $teacherStartBtn.addClass("d_none"); // 교사 시작 버튼 부모요소 숨김(WAITING 상태 외 숨김)
        // 학생 관련
        $waitingButton.addClass("d_none"); // 학생 대기 표시 숨김(WAITING 상태 외 숨김)
        $studentControls.removeClass("d_none"); // 학생 질문/정답 제출 버튼 부모요소 노출(WAITING 상태 외 노출)
        $raiseHandBtn.addClass("d_none"); // 손들기 버튼 숨김(IN_PROGRESS 상태 외 숨김)

        // 위 배열에 아래 요소들 추가해도 됨
        // $questionInput.addClass('disabled');
        // $answerInput.addClass('disabled');

        // 상태별
        switch (roomStatus) {
            case 'WAITING': // 기본 방 상태, 학생 입장 시 아직 교사가 시작 버튼을 누르지 않았을 때
                // 교사 관련
                $teacherStartBtn.removeClass("disabled d_none"); // 교사 시작 버튼 활성화
                $teacherControls.addClass("d_none"); // 교사 OX 버튼 부모요소 숨김
                // 학생 관련
                $waitingButton.removeClass("d_none"); // 학생 대기 표기
                $studentControls.addClass("d_none"); // 학생 질문/정답 제출 버튼 부모요소 숨김
                break;
            case 'IN_PROGRESS':
                // 학생 관련
                $studentControls.addClass("d_none"); // 학생 질문/정답 제출 버튼 부모요소 숨김
                $raiseHandBtn.removeClass("d_none"); // 손들기 버튼 활성화
                break;
            case 'AWAITING_INPUT':
                if (currentUserNo === winnerNo) { // 자바스크립트에서 == 는 완벽한 비교가 아님으로 === 사용
                    $sendQuestionBtn.removeClass('disabled');
                    $sendAnswerBtn.removeClass('disabled');
                    $twentyUserAnswerForm.removeClass('disabled');
                }
                $raiseHandBtn.addClass("d_none");
                break;
            case 'AWAITING_RESPONSE':
                $teacherObtn.removeClass('disabled');
                $teacherXbtn.removeClass('disabled');
                break;
            default:
                break;
        }
    }

    // 웹소켓 연결 및 구독
    function connectWebSocket() {
        console.log("connectWebSocket() ws: ", wsUrl);

        const socket = new SockJS('http://103.218.158.164:30081/ws?token=' + ACCESS_TOKEN);
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
             * 5.getParticipantListHtml(userList) : - 참여자 리스트를 전달 받아, 이 리스트를 화면에 띄워주는 메소드
             *                                     - 참여자의 상태에 따라, 들어왔을 땐 초록색, 나갔을 땐 회색으로 표시
             *
             * 6.updateBtnByRoomStatus(data) : - data를 전달 받아, 메소드 내에서 data.roomStatus를 꺼내,
             *                                 - 방의 상태에 따라 버튼의 활성화 여부(disabled 처리)를 설정함.
             */
            stompClient.subscribe(`/topic/participants/${currentRoomNo}`, function (result) {
                const data = JSON.parse(result.body);       // 서버에서 전달 받은 값을 꺼내고
                // updateParticipantList(data.list);           // list의

                // 가공된 값을 반환 받아 호출부에서 처리
                // empty + append -> html (같은 기능 구사)
                $participantsList.html(getParticipantListHtml(data.list));
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
             * 5. updateBtnByRoomStatus(data) : - data를 전달 받아, 메소드 내에서 data.roomStatus를 꺼내,
             *                                  - 방의 상태에 따라 버튼의 활성화 여부(disabled 처리)를 설정함.
             */
            stompClient.subscribe(`/topic/gameStart/${currentRoomNo}`, function (result) {
                const data = JSON.parse(result.body);

                // appendBoardLine(data); // 시스템만 들어 있음
                // 필요한 값만 전달하여 활용 여기서 분기처리해도 되고 해당 함수 내에도 분기처리 함
                // data.system이 null인지
                showSystemMessage(data.system);

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
                $turnHolderName.text(data.name);
                $turnInfoPanel.show();

                // 매번 제이쿼리 선택객체 생성으로 제이쿼리 큐에 계속 누적되어 브라우저 메모리 차지로 느려지거나 버벅거림 현상 발생할 수 있음
                // $("#turn-holder-name").text(data.name); // 상단부에 미리 선언
                // $("#turn-info-panel").show(); // 상단부에 미리 선언

                startVisualTimer(data.time);

                // msgCnt가 19 이상이면, 정답 버튼만 활성화, 아니면 그냥 원래대로 활성화
                if (data.msgCnt >= 19) {
                    // appendBoardLine(data); // 시스템만 사용됨
                    showSystemMessage(data.system);

                    // disabled 스타일 통일을 위해 class로 제어
                    $studentControls.removeClass("d_none"); // 학생 질문/정답 제출 버튼 부모요소 노출
                    $raiseHandBtn.addClass("d_none"); // 손들기 버튼 숨김
                    $twentyUserAnswerForm.removeClass("disabled");
                    $sendAnswerBtn.removeClass("disabled");
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
                stopVisualTimer(); // 타이머 제거 메소드

                // appendBoardLine(data1);          // 메세지 UI 표현(단일 메세지와 시스템은 없음)
                const chatListHtml = getChatListHtml(data1);
                $chatsBoard.append(chatListHtml);
                $chatsBoard.scrollTop($chatsBoard[0].scrollHeight);
            });

            /** 교사 서버 끊겼을 때, 알림창이 나오고 그룹 페이지로 이동.
             * 1. 교사가 서버가 끊기거나, 웹브라우저 탭을 닫을 경우 (게임 종료 버튼 누를 때 x)
             * 2. 경고창을 띄우고, 그룹 페이지로 이동함.
             */
            stompClient.subscribe(`/topic/TeacherDisconnect`, function () {
                // const data = {
                //     system : "스무고개가 중단되었습니다. 잠시 후에 이동합니다..."
                // };
                // appendBoardLine(data) // 시스템만 있음

                showSystemMessage("스무고개가 중단되었습니다. 잠시 후에 이동합니다.");

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
             */
            stompClient.subscribe(`/topic/TeacherResponce`, function (result) {
                const data = JSON.parse(result.body);

                if (data.var != null) { // 게임이 끝난 경우
                    // let message;
                    // // 스무고개 승리 여부에 따른 안내 메세지를 구성해서 띄워주고,
                    // if(data.teacherResponse === "Y" ) {
                    //     message = {
                    //         system : "승리하셨습니다! 종료를 눌러주세요"
                    //     }
                    // } else {
                    //     message = {
                    //         system : "패배 하셨습니다.. 종료를 눌러주세요"
                    //     }
                    // }
                    // appendBoardLine(message);  // 이게 안내 메세지 띄워주는 메소드

                    showSystemMessage(data.teacherResponse === "Y"
                        ? "승리하셨습니다! 종료를 눌러주세요"
                        : "패배 하셨습니다.. 종료를 눌러주세요");

                    $teacherObtn.addClass('disabled');   //O,X 버튼 비활성화 해주고
                    $teacherXbtn.addClass('disabled');

                    renderMessageList(data.msgList);  // 이 게임방의 전체 메세지 리스트를 다시 갱신 해준다.
                    return;
                }
                // 게임이 끝나지 않고 계속 재개해야하는 경우
                updateBtnByRoomStatus(data);
                renderMessageList(data.msgList);
            });

            /**
             * 입장 할 때 마다, 사용자 정보를 보냄.
             */
            stompClient.send(`/app/join/${currentRoomNo}`, {}, JSON.stringify({}));
        });
    }
    connectWebSocket(); // 실행

    // 참여자 리스트 비동기 요청하여 랜더링
    function getMsgListByRoomNo() {
        console.log("getMsgListByRoomNo()");

        $.getJSON(`/api/twenty/getMsgList/` + currentRoomNo, function (msgListData){
            let msgList = msgListData.data;
            console.log(msgList);
            renderMessageList(msgList);
        });
    }
    getMsgListByRoomNo(); // 실행

    // 단계 선택 및 파일 선택 모달창 열기
    const $modalTwentyBtn = $(".modal_twenty_btn"); // 종료 모달창 열기 버튼
    const $modalTwentyExit = $("#modal-twenty-exit"); // 스무고개 종료 모달창
    $modalTwentyBtn.click(async function () {
        if ($modalTwentyExit.length) {
            // 해당 모달창 요소가 있을 경우 열기
            $modalTwentyExit.show();

            // 약간의 딜레이를 주어 show 후 css transition 적용될 수 있도록 함
            setTimeout(() => {
                $modalTwentyExit.addClass("open");
            }, 10);
        }
    });

});