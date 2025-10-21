/**
 * 통합 퀴즈 핸들러
 * 퀴즈의 상태(state)와 비즈니스 로직을 관리합니다.
 * 이 파일은 DOM에 직접 접근하지 않습니다.
 */

// 퀴즈 상태를 관리하는 객체
export const quizState = {
    currentQuizIndex: 0,
    selectedAnswers: [], // 사용자가 선택한 답안 배열
    usedHints: [],       // 힌트 사용 여부 배열 (Level 모드 전용)
    quizStartTime: null, // 퀴즈 시작 시간
};

/**
 * 퀴즈 상태를 초기화합니다.
 * @param {number} totalQuizzes - 전체 퀴즈 개수
 */
export function initializeQuizState(totalQuizzes) {
    quizState.currentQuizIndex = 0;
    quizState.selectedAnswers = new Array(totalQuizzes);
    quizState.usedHints = new Array(totalQuizzes).fill(false);
    quizState.quizStartTime = Date.now();
    console.log("퀴즈 상태가 초기화되었습니다.");
}

/**
 * 사용자의 답안을 기록합니다.
 * @param {number} choiceNumber - 사용자가 선택한 보기 번호 (1-based)
 */
export function selectAnswer(choiceNumber) {
    quizState.selectedAnswers[quizState.currentQuizIndex] = choiceNumber;
    console.log(`[State] 문제 ${quizState.currentQuizIndex + 1} 답안 ${choiceNumber} 선택됨`);
}

/**
 * 힌트 사용 상태를 기록합니다.
 */
export function recordHintUsage() {
    quizState.usedHints[quizState.currentQuizIndex] = true;
    console.log(`[State] 문제 ${quizState.currentQuizIndex + 1} 힌트 사용됨`);
}

/**
 * 다음 문제로 인덱스를 이동합니다.
 * @returns {boolean} 다음 문제로 이동 성공 여부
 */
export function goToNext() {
    // quizzes 변수가 이 파일에 없으므로, quiz.js에서 quizzes.length를 사용합니다.
    // 이 함수는 외부에서 quizzes.length를 기준으로 호출되어야 합니다.
    quizState.currentQuizIndex++;
    return true;
}

/**
 * 정답이 아닌 보기 중 하나를 랜덤하게 선택합니다.
 * @param {Object} quiz - 현재 퀴즈 객체
 * @returns {number} 제거할 보기 번호 (1-based)
 */
export function getRandomWrongOption(quiz) {
    const correctAnswer = quiz.successAnswer;
    const wrongOptions = [];

    // 정답이 아닌 보기 수집
    for (let i = 1; i <= quiz.options.length; i++) {
        if (i !== correctAnswer) {
            wrongOptions.push(i);
        }
    }

    // 랜덤 선택
    const randomIndex = Math.floor(Math.random() * wrongOptions.length);
    return wrongOptions[randomIndex];
}

/**
 * 퀴즈 제출을 위한 최종 결과 데이터를 생성합니다.
 * @param {string} quizMode - 현재 퀴즈 모드 ('speed' 또는 'level')
 * @param {Array} quizzesData - 전체 퀴즈 데이터 배열
 * @returns {Object} - form 제출에 필요한 데이터 객체
 */
export function createResultPayload(quizMode, quizzesData) {
    const isSpeedMode = quizMode === 'speed';
    const totalTimeSec = isSpeedMode
        ? Math.floor((Date.now() - quizState.quizStartTime) / 1000)
        : 0;

    const results = quizzesData.map((quiz, idx) => {
        const result = {
            quizNo: quiz.quizNo,
            selectedChoice: quizState.selectedAnswers[idx] ?? 0,
            isBookmarked: "N",
        };

        if (quizMode === 'level') {
            result.usedHint = quizState.usedHints[idx] ? "Y" : "N";
            if (result.usedHint === "Y" && quiz.hintRemovedOption) {
                result.hintRemovedOption = quiz.hintRemovedOption;
            }
        }
        return result;
    });

    return {
        totalTimeSec,
        quizMode,
        results,
    };
}
