import {apiErrorProcessByXhr} from '../../utils/api-error-util.js';

/**
 * 전달 받은 월에 해당하는 사용자 랭크 정보 포함 랭크 정보 목록 비동기 요청
 * @param {string} rankMonth - "yyyy-MM" 형식
 * @returns {Promise<*>} 랭크 정보 목록 데이터
 */
export async function apiGetRanks(rankMonth) {
    console.log(`apiGetRanks() api 요청 실행 rankMonth: ${rankMonth}`);
    try {
        const response = await $.ajax({
            method: "GET",
            url: "/api/ranks",
            data: {rankMonth},
            dataType: "json",
        });

        console.log("apiGetRanks() api 요청 response: ", response);
        return response.data; // json 으로 변환된 Map 객체
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}