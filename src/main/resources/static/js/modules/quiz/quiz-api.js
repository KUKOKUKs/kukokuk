import {apiErrorProcessByXhr} from '../../utils/api-error-util.js';

/**
 * 힌트 사용 API 호출
 * @param {number} quizIndex - 퀴즈 인덱스
 * @param {number} removedOption - 제거된 보기 번호
 * @returns {Promise<{success: boolean, data: number}>} 남은 힌트 개수
 */
export async function apiUseHint(quizIndex, removedOption) {
    console.log("apiUseHint() api 요청 실행", {quizIndex, removedOption});

    try {
        const response = await $.ajax({
            url: "/api/quiz/hints",
            method: "POST",
            data: {
                quizIndex: quizIndex,
                removedOption: removedOption
            },
            dataType: "json"
        });

        console.log("apiUseHint() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
        throw xhr;
    }
}

/**
 * 퀴즈 결과 제출 API
 * @param {Object} resultData - 퀴즈 결과 데이터
 * @returns {Promise<*>} 제출 응답
 */
export async function apiSubmitQuizResults(resultData) {
    console.log("apiSubmitQuizResults() api 요청 실행");

    try {
        const response = await $.ajax({
            url: "/api/quiz/results",
            method: "POST",
            data: JSON.stringify(resultData),
            contentType: "application/json",
            dataType: "json"
        });

        console.log("apiSubmitQuizResults() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
        throw xhr;
    }
}