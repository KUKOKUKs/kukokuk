// noinspection ES6UnusedImports

import {apiErrorProcessByXhr} from '../../utils/api-error-util.js';

// 학습 단계 정보 비동기 요청
export async function getStudyDifficultyList() {
    console.log("getStudyDifficultyList() api 요청 실행");
    try {
        const response = await $.ajax({
            method: "GET",
            url: "/api/studies/difficulties",
            dataType: "json",
        });

        console.log("getStudyDifficultyList() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}

export async function getDailyStudies(rows) {
    console.log("getDailyStudies() api 요청 실행");
    try {
        const response = await $.ajax({
            method: 'GET',
            url: '/api/studies',
            contentType: 'application/json',
            data: {'rows': rows},
            dataType: 'json',
        });

        console.log("getDailyStudies() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }

}