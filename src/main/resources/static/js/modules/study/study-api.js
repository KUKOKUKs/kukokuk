// noinspection ES6UnusedImports

import {apiErrorProcessByXhr} from '../../utils/api-error-util.js';
import {
    renderStudyListCard,
    renderStudyListCardFirstSkeleton
} from "./study-renderer.js";
import {pollStudyJob} from "./study-poll.js";

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

// /**
//  * 조회 행 개수를 전달 받아 사용자 맞춤 학습자료 목록을 조회하는 비동기 요청
//  * @param rows 조회할 학습자료 개수
//  * @returns {Promise<*>} [
//  *    {
//  *      "dailyStudyNo": 1,
//  *      "title": "문단 배우기: 중심 문장과 뒷받침 문장",
//  *      "cardCount" : 3, // 일일학습의 총 카드 개수
//  *      "status" : "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
//  *      "studiedCardCount" : 2, // 해당 사용자가 이 일일학습에서 학습한 카드 개수
//  *      "progressRate" : 66,
//  *      "school" : "초등", // "초등", "중등",
//  *      "grade" : 1,
//  *      "sequence" : 3 // 학년 내 자료의 순서
//  *    }
//  *  ]
//  */
// export async function apiGetDailyStudiesSync(rows) {
//     console.log("apiGetDailyStudies() api 요청 실행");
//     try {
//         const response = await $.ajax({
//             method: 'GET',
//             url: `/api/studies`,
//             contentType: 'application/json',
//             data: {'rows': rows},
//             dataType: 'json',
//         });
//
//         console.log("apiGetDailyStudies() api 요청 response: ", response);
//         return response.data;
//     } catch (xhr) {
//         apiErrorProcessByXhr(xhr.responseJSON);
//         throw xhr; // 반드시 다시 throw 해줘야 상위에서 catch 가능
//     }
// }

/**
 * 요청할 자료 수를 전달받아 맞춤 학습 자료 비동기 요청
 * <p>
 *     List<>JobStatusResponse<DailyStudySummaryResponse>> 객체 응답으로
 *     폴링 처리 하여 진행 상태 확인
 * @param rows 조회할 학습자료 개수
 * @param $studyListContainer 진행률이 표시될 부모 요소
 * @returns 맞춤 학습 자료 목록
 */
export async function apiGetDailyStudies(rows, $studyListContainer) {
    console.log("apiGetHomeUserDailyStudies() api 요청 실행");

    try {
        // 최초 요청
        const response = await $.ajax({
            method: "GET",
            url: "/api/studies",
            data: {rows},
            dataType: "json",
        });

        console.log("apiGetHomeUserDailyStudies() api 요청 response: ", response);

        // DONE일 경우 바로 렌더링
        console.log(response.data);

        // 동작을 기다리지 않고 응답
        await pollAndRenderJobStatusList(response.data, $studyListContainer);

        // 최초 응답
        return response.data; // JobStatusResponse의 리스트
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}

/**
 * Job 상태 목록을 순회하며 즉시 렌더링 + 폴링 처리
 * @param jobStatusList JobStatusResponse[]
 * @param $studyListContainer jQuery 컨테이너
 */
export async function pollAndRenderJobStatusList(jobStatusList, $studyListContainer) {
    $studyListContainer.empty();

    // 모든 스켈레톤 카드 먼저 렌더링
    // jobStatusList.forEach(job => {
    //     renderStudyListCardFirstSkeleton(job.jobId, $studyListContainer);
    // });
    
    // 유연하고 빠르고 안정적이게 적용
    const skeletionHtml = renderStudyListCardFirstSkeleton(jobStatusList);
    $studyListContainer.html(skeletionHtml);

    // forEach문은 비동기처리(await)를 기다려주지 않음
    // forEach는 콜백을 호출만 하고, 콜백 안의 비동기 처리 결과를 Promise로 모아서 기다리는 로직이 없다
    for (const [index, job] of jobStatusList.entries()) { // entries() : 인덱스와 값을 동시에 꺼냄
        // 이미 데이터가 존재하는 경우, 바로 렌더링
        if (job.status === "DONE") {
            renderStudyListCard(job, index, $studyListContainer);
        }

        // 폴링 시작 -> 상태 DONE/FAILED 되면 다시 renderStudyListCard 호출
        else if (job.status === "PROCESSING") {
            const $studyCardContainer = $studyListContainer.find(`[data-job-id="${job.jobId}"]`);
            try {
                // index 순서대로 순차적으로 폴링을 처리하고, job이 DONE이되고 Promise가 resolve되면 다음 동작 실행
                const updatedJob = await pollStudyJob(job.jobId, $studyCardContainer);
                renderStudyListCard(updatedJob, index, $studyListContainer);
            } catch (err) {
                console.error(`jobId=${job.jobId} 실패`, err);
                job.status = "FAILED";
                renderStudyListCard(job, index, $studyListContainer);
            }
        }
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
            data: JSON.stringify(requestBody), // 바디 비워서 updatedDate만 수정
            dataType: 'json',
        });

        console.log("apiUpdateStudyLog() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
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
    }
}