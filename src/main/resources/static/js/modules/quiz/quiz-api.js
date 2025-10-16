import {apiErrorProcessByXhr} from '../../utils/api-error-util.js';

/**
 * 통합 퀴즈 API 모듈
 * 서버와의 데이터 통신(AJAX)을 담당합니다.
 */

/**
 * 힌트 사용 API를 호출합니다.
 * 서버에 힌트 사용 내역을 전송하고, 남은 힌트 개수를 받아옵니다.
 * @param {number} quizIndex - 현재 퀴즈의 인덱스
 * @param {number} removedOption - 힌트로 인해 제거된 보기 번호
 * @returns {Promise<number>} 성공 시 남은 힌트 개수를 반환하는 Promise
 */
export async function apiUseHint(quizIndex, removedOption) {
    console.log(`apiUseHint() API 요청 실행 quizIndex: ${quizIndex}, removedOption: ${removedOption}`);
    try {
        const response = await $.ajax({
            url: "/quiz/use-hint",
            method: "POST",
            data: {
                quizIndex: quizIndex,
                removedOption: removedOption
            },
            dataType: "json"
        });

        console.log("apiUseHint() API 요청 response:", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}