/**
 * 정답이 아닌 보기 중 랜덤 선택
 * @param {Object} quiz - 퀴즈 객체
 * @returns {number} 랜덤 선택된 오답 보기 번호
 */
export function getRandomWrongOption(quiz) {
    console.log("getRandomWrongOption() 실행");

    const correctAnswer = quiz.successAnswer;
    const wrongOptions = [];

    for (let i = 1; i <= quiz.options.length; i++) {
        if (i !== correctAnswer) {
            wrongOptions.push(i);
        }
    }

    const randomIndex = Math.floor(Math.random() * wrongOptions.length);
    return wrongOptions[randomIndex];
}

/**
 * 퀴즈 결과 데이터 생성
 * @param {Array} quizzes - 퀴즈 배열
 * @param {Array} selectedAnswers - 선택한 답안 배열
 * @param {Array} usedHints - 힌트 사용 배열 (Level 모드)
 * @param {string} quizMode - 퀴즈 모드
 * @param {number} totalTimeSec - 총 소요 시간 (Speed 모드)
 * @returns {Object} 결과 데이터 객체
 */
export function buildQuizResultData(quizzes, selectedAnswers, usedHints, quizMode, totalTimeSec) {
    console.log("buildQuizResultData() 실행");

    const results = quizzes.map((quiz, idx) => {
        const result = {
            quizNo: quiz.quizNo,
            selectedChoice: selectedAnswers[idx] ?? 0,
            isBookmarked: "N"
        };

        // Level 모드 전용 데이터
        if (quizMode === "level" && usedHints) {
            result.usedHint = usedHints[idx] ? "Y" : "N";
            if (usedHints[idx] && quiz.hintRemovedOption) {
                result.hintRemovedOption = quiz.hintRemovedOption;
            }
        }

        return result;
    });

    return {
        quizMode: quizMode,
        totalTimeSec: totalTimeSec || 0,
        results: results
    };
}

/**
 * FormData 생성 헬퍼
 * @param {Object} data - 폼 데이터 객체
 * @returns {FormData} FormData 객체
 */
export function createFormData(data) {
    const formData = new FormData();

    // CSRF 토큰 추가
    const csrfToken = $("meta[name='_csrf']").attr("content");
    if (csrfToken) {
        formData.append("_csrf", csrfToken);
    }

    // 데이터 추가
    Object.keys(data).forEach(key => {
        if (typeof data[key] === 'object' && !Array.isArray(data[key])) {
            formData.append(key, JSON.stringify(data[key]));
        } else {
            formData.append(key, data[key]);
        }
    });

    return formData;
}