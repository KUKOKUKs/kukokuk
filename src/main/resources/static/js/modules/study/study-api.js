// noinspection ES6UnusedImports

import {apiErrorProcessByXhr} from '../../utils/api-error-util.js';

// 학습 단계 정보 비동기 요청
export async function apiGetStudyDifficultyList() {
    console.log("apiGetStudyDifficultyList() api 요청 실행");
    try {
        const response = await $.ajax({
            method: "GET",
            url: "/api/studies/difficulties",
            dataType: "json",
        });

        console.log("apiGetStudyDifficultyList() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}

/**
 * 조회 행 개수를 전달 받아 사용자 맞춤 학습자료 목록을 조회하는 비동기 요청
 * @param rows 조회할 학습자료 개수
 * @returns {Promise<*>} [
 *    {
 *      "dailyStudyNo": 1,
 *      "title": "문단 배우기: 중심 문장과 뒷받침 문장",
 *      "cardCount" : 3, // 일일학습의 총 카드 개수
 *      "status" : "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
 *      "studiedCardCount" : 2, // 해당 사용자가 이 일일학습에서 학습한 카드 개수
 *      "progressRate" : 66,
 *      "school" : "초등", // "초등", "중등",
 *      "grade" : 1,
 *      "sequence" : 3 // 학년 내 자료의 순서
 *    }
 *  ]
 */
export async function apiGetDailyStudies(rows) {
    console.log("apiGetDailyStudies() api 요청 실행");
    try {
        const response = await $.ajax({
            method: 'GET',
            url: `/api/studies`,
            contentType: 'application/json',
            data: {'rows': rows},
            dataType: 'json',
        });

        console.log("apiGetDailyStudies() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
        throw xhr; // 반드시 다시 throw 해줘야 상위에서 catch 가능
    }
}

/**
 * 학습 이력을 생성하는 비동기 요청
 * @param dailyStudyNo 학습이력을 생성할 학습자료 번호
 * @returns {Promise<*>} {
 *        "dailyStudyLogNo": 6,
 *         "studiedCardCount": 6,
 *         "completedDate": "2025-07-31T16:41:42",
 *         "status": "COMPLETED",
 *         "createdDate": "2025-07-31T15:36:08",
 *         "updatedDate": "2025-07-31T16:41:42",
 *         "userNo": 2,
 *         "dailyStudyNo": 9,
 *         "dailyStudy": null
 *     }
 */
export async function apiCreateStudyLog(dailyStudyNo) {
    try {
        const response = await $.ajax({
            url: `/api/studies/logs`,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ dailyStudyNo }),
            dataType: 'json',
        });

        console.log("apiCreateStudyLog() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
        throw xhr;
    }
}

/**
 * 학습 이력을 수정하는 비동기 요청
 * @param studyLogNo 수정할 학습 이력의 번호
 * @param requestBody 수정할 필드 {studiedCardCount, status}
 * @returns {Promise<*>} {
 *     "dailyStudyLogNo": 6,
 *     "studiedCardCount": 6,
 *     "completedDate": "2025-07-31T16:41:42",
 *     "status": "COMPLETED",
 *     "createdDate": "2025-07-31T15:36:08",
 *     "updatedDate": "2025-07-31T16:41:42",
 *     "userNo": 2,
 *     "dailyStudyNo": 9,
 *     "questCompleted": true
 *   }
 */
export async function apiUpdateStudyLog(studyLogNo, requestBody = {}) {
    try {
        const response = await $.ajax({
            url: `/api/studies/logs/${studyLogNo}`,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({}), // 바디 비워서 updatedDate만 수정
            dataType: 'json',
        });

        console.log("apiUpdateStudyLog() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
        throw xhr;
    }
}

/**
 * 학습 퀴즈 이력을 생성하는 비동기 요청
 * @param dailyStudyQuizNo
 * @param selectedChoice
 * @returns {Promise<*>} {
 *         "dailyStudyQuizLogNo": 1,
 *         "isSuccess": "N",
 *         "selectedChoice": 3,
 *         "createdDate": "2025-08-01T10:13:14",
 *         "updatedDate": "2025-08-01T10:35:45",
 *         "userNo": 2,
 *         "dailyStudyQuizNo": 6,
 *         "dailyStudyQuiz": null
 *     }
 */
export async function apiCreateQuizLog(dailyStudyQuizNo, selectedChoice) {
    try {
        const response = await $.ajax({
            url: '/api/studies/quizzes/logs',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                dailyStudyQuizNo,
                selectedChoice
            }),
            dataType: 'json',
        });

        console.log("apiCreateQuizLog() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
        throw xhr;
    }
}

/**
 * 학습 퀴즈 이력을 수정하는 비동기 요청
 * @param studyQuizLogNo
 * @param selectedChoice
 * @returns {Promise<*>} {
 *         "dailyStudyQuizLogNo": 1,
 *         "isSuccess": "N",
 *         "selectedChoice": 3,
 *         "createdDate": "2025-08-01T10:13:14",
 *         "updatedDate": "2025-08-01T10:35:45",
 *         "userNo": 2,
 *         "dailyStudyQuizNo": 6,
 *         "dailyStudyQuiz": null
 *     }
 */
export async function apiUpdateQuizLog(studyQuizLogNo, selectedChoice) {
    try {
        const response = await $.ajax({
            url: `/api/studies/quizzes/logs/${studyQuizLogNo}`,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({
                selectedChoice
            }),
            dataType: 'json',
        });

        console.log("apiUpdateQuizLog() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
        throw xhr;
    }
}

/**
 * 서술형 퀴즈 이력 생성
 */
export async function apiCreateEssayQuizLog(dailyStudyEssayQuizNo, userAnswer) {
    try {
        const response = await $.ajax({
            url: '/api/studies/essays/logs',
            method: 'POST',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({
                dailyStudyEssayQuizNo,
                userAnswer
            }),
        });

        return response;
    } catch (xhr) {
        console.error("서술형 퀴즈 이력 생성 실패:", xhr);
        throw xhr;
    }
}

/**
 * 서술형 퀴즈 이력 수정
 */
export async function apiUpdateEssayQuizLog(essayQuizLogNo, dailyStudyEssayQuizNo, userAnswer) {
    try {
        const response = await $.ajax({
            url: `/api/studies/essays/logs/${essayQuizLogNo}`,
            method: 'PUT',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({
                dailyStudyEssayQuizNo,
                userAnswer
            }),
        });

        return response;
    } catch (xhr) {
        console.error("서술형 퀴즈 이력 수정 실패:", xhr);
        throw xhr;
    }
}

/**
 * AI 피드백생성 비동기 요청
 * @param essayQuizLogNo
 * @param dailyStudyEssayQuizNo
 * @param userAnswer
 * @returns {Promise<*>}
 * "sections": [
 * {
 * "type": "summary",
 * "title": "총평",
 * "items": [
 * {
 * "extra": {"icon": '👍'},
 * "text": "답변이 매우 짧고, 문제에서 요구하는 내용을 거의 포함하지 못했습니다. …."
 */
export async function apiRequestEssayFeedback(essayQuizLogNo, dailyStudyEssayQuizNo, userAnswer) {
    try {
        const requestBody = {
            dailyStudyEssayQuizLogNo: essayQuizLogNo, // null 가능
            dailyStudyEssayQuizNo,
            userAnswer
        };

        const response = await $.ajax({
            url: '/api/studies/essays/ai',
            method: 'PUT',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify(requestBody),
        });


        return response.data;
    } catch (xhr) {
        console.error("AI 피드백 요청 실패:", xhr);
        throw xhr;
    }
}