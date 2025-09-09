import {renderStudyListCard} from "./study-renderer";
import {apiGetDailyStudies} from "./study-api";

/**
 * 학습 자료 목록 조회 후 화면에 스켈레톤 렌더링 및 폴링 요청을 비동기로 수행하는 메소드
 * @param rows
 * @param $studyListContainer
 * @returns {Promise<*>}
 */
export async function renderFirstAndPoll(rows, $studyListContainer) {

    // 학습자료 목록 작업상태를 조회하는 비동기 메소드 호출
    const jobStatuses = apiGetDailyStudies(rows);

    let content = '';

    // jobStatuses 반복하면서 DONE인 데이터는 바로 렌더링, PROCESSING 데이터는 스켈레톤 표시 및 비동기로 폴링 처리
    jobStatuses.forEach((jobStatus, index) => {
        const jobId = jobStatus.jobId;

        // DONE이면 바로 렌더링
        if (jobStatus.status === "DONE") {
            console.log("이미 완료된 상태로 DB데이터 반환:", jobStatus.result);
            content += renderStudyListCard(jobStatus.result, index);
        }

        // PROCESSING이면 스켈레톤 표시 및 폴링
        else if (jobStatus.status === "PROCESSING") {

            // 스켈레톤 추가
            content += renderStudyListCardSkeleton(index);

            // 폴링 비동기 실행(기다리지 않음)
        }
    });

    // 기존데이터 + 스켈레톤을 화면에 우선 렌더링
    $studyListContainer.append(content);
}