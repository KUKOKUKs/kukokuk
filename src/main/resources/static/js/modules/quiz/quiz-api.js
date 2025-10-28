import {apiErrorProcessByXhr} from '../../utils/api-error-util.js';

/**
 * 북마트 추가/제거 비동기 요청
 * @param quizNo 북마크 추가/제거할 퀴즈 번호
 * @param isBookmarked 현재 북마트 상태
 * @returns {Promise<*>} 성공여부
 */
export async function apiToggleBookmark(quizNo, isBookmarked) {
    console.log(`apiToggleBookmark() 실행 quizNo: ${quizNo}, isBookmarked: ${isBookmarked}`);
    try {
        const response = await $.ajax({
            url: `/api/quiz/bookmark/${quizNo}`,
            method: `${isBookmarked ? 'DELETE' : 'POST'}`,
            dataType: "json"
        });

        console.log("apiToggleBookmark() API 요청 response:", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}

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