// noinspection ES6UnusedImports

import {apiErrorProcessByXhr} from '../../utils/api-error-util.js';

/**
 * 사용자의 일일 도전과제 보상 획득 처리 및 획득 후 힌트 개수 요청
 * @param dailyQuestUserNo 일일 도전과제 완료 내역 번호
 * @returns 업데이트 후 힌트 개수
 */
export async function apiPutDailyQuestUserObtainReward(dailyQuestUserNo) {
    console.log("apiPutDailyQuestUserObtainReward() api 요청 실행");
    try {
        const response = await $.ajax({
            method: "PUT",
            url: `/api/daily-quest-user/${dailyQuestUserNo}/obtain`,
            dataType: "json",
        });

        console.log("apiPutDailyQuestUserObtainReward() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}

/**
 * 사용자에 대한 모든 퀘스트와 진행도 및 보상 획득여부 정보를 포함한 목록 비동기 요청
 * 미인증 시 기본 퀘스트 목록 비동기 요청
 * @returns 퀘스트와 진행도 및 보상 획득여부 정보를 포함한 목록 / 기본 퀘스트 목록(미인증 시)
 */
export async function apiGetDailyQuestList(isLoggedIn) {
    console.log("apiGetDailyQuestList() api 요청 실행");
    try {
        const response = await $.ajax({
            method: "GET",
            url: isLoggedIn ? "/api/daily-quests" : "/api/daily-quests/basic",
            dataType: "json",
        });

        console.log("apiGetDailyQuestList() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}
