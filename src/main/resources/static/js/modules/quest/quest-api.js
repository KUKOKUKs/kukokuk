// noinspection ES6UnusedImports

import {apiErrorProcessByXhr} from '../../utils/api-error-util.js';

// 일일도전과제 정보 비동기 요청
export async function getDailyQuestList() {
    console.log("getDailyQuestList() api 요청 실행");
    try {
        const response = await $.ajax({
            method: "GET",
            url: "/api/studies/difficulties",
            dataType: "json",
        });

        console.log("getDailyQuestList() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}