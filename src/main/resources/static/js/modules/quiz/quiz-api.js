/**
 * 통합 퀴즈 API 모듈
 * 서버와의 데이터 통신(AJAX)을 담당합니다.
 */

/**
 * 힌트 사용 API를 호출합니다.
 * 서버에 힌트 사용 내역을 전송하고, 남은 힌트 개수를 받아옵니다.
 * @param {number} quizIndex - 현재 퀴즈의 인덱스
 * @param {number} removedOption - 힌트로 인해 제거된 보기 번호
 * @returns {Promise<Object>} 성공 시 서버의 응답 객체를, 실패 시 에러를 반환하는 Promise
 */
export async function useHintApi(quizIndex, removedOption) {
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
        console.log("힌트 사용 API 응답:", response);
        return response;
    } catch (error) {
        console.error("힌트 사용 API 호출에 실패했습니다.", error);
        throw error; // 에러를 상위로 전파하여 호출한 곳에서 처리하도록 함
    }
}
