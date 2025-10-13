import {apiGetDailyStudies} from "./study-api.js";
import {renderStudyCard, renderStudyListSkeleton} from "./study-renderer.js";
import {pollJobStatus} from "../../utils/poll-job-utils.js";

$(document).ready(async () => {
    /**
     * 프레그먼츠에 추가할 학습 자료 요청(학습 자료 개수: 컨트롤러 기본 값 5)
     * 최초 요청 후 폴링하며 진행 상태 표시
     * 청시 기본 5개로 요청이 되며 studyRows 만큼만 활용 됨
     * 예외사항으로는 studyRows가 컨트롤러 기본 값 5보다 클 경우엔 studyRows만큼 요청
     * !! 사용할 데이터 개수 외로 미리 생성되도록 하여 사용자 경험 향상 !!
     */
    // 프레그먼츠로 사용되는 학습 자료 관련
    const $studyListContainer = $('#study-list-container'); // 학습 자료가 생성될 부모 요소
    const studyRows = Number($studyListContainer.data("study-rows") || 1); // 사용할 학습 자료 개수
    const requestRows = studyRows > 5 ? studyRows : 5; // 기본 요청 데이터 개수는 컨트롤러 기본 값 5
    const isUseSpinner = Boolean($studyListContainer.data("is-spinner") || false); // 진행 상태 스피너 로딩 사용여부(기본 스켈레톤 로딩)

    // 학습 자료 요청하여 프레그먼츠에 추가
    async function renderFragmentDailyStudies() {
        console.log("renderFragmentDailyStudies() 실행");

        try {
            // 최초 요청에 대한 응답
            // List<JobStatusResponse<DailyStudySummaryResponse>>> 객체
            const jobStatusList = await apiGetDailyStudies(requestRows);

            // 실제 활용할 데이터만 추출 (앞에서부터 studyRows개)
            const usableJobStatusList = jobStatusList.slice(0, studyRows);
            console.log("사용할 데이터만 추출 usableJobStatusList: ", usableJobStatusList);
            $studyListContainer.html(renderStudyListSkeleton(usableJobStatusList)); // 스켈레톤 로딩 세팅 (data-job-id 속성 추가)

            /**
             * usableJobStatusList 데이터로 임시로 생성한 스켈레톤에 랜더링 및 폴링
             * job.status가 DONE/FAILED (완료/실패) 상태인 job은 즉시 랜더링
             * DONE/FAILED이 아닌 PROCESSING 상태인 job은 순차적으로 폴링 요청
             * -> 순차적으로 폴링 요청하지만 병렬 비동기 호출로 먼저 DONE/FAILED 처리된 job은 즉시 랜더링하여
             *    사용자 경험 향상 (로딩 속도 빠르게 느껴지도록)
             */
            for (const [index, job] of usableJobStatusList.entries()) {
                // 해당하는 학습 카드 찾기
                const $studyCard = $studyListContainer.find(`[data-job-id="${job.jobId}"]`);
                if (!$studyCard.length) continue; // 해당 요소가 없다면 다음 루프

                if (job.status !== "PROCESSING") {
                    // 현재 job의 상태가 PROCESSING이 아닐 경우 완료/실패로 판단하여 즉시 랜더링
                    renderStudyCard(job, index, $studyCard); // job 객체, 첫 번째 요소 확인 값, 랜더링 요소
                } else {
                    // 현재 job의 상태가 PROCESSING일 경우 폴링 요청
                    // 폴링은 각자 독립적으로 진행(병렬 비동기 호출)하기 위해 await을 사용하지 않음
                    // (void로 Promise 무시 의도 명시/IDE 경고 제거)
                    void pollStudyJobStatus(job, index, $studyCard);
                }
            }
        } catch (error) {
            // 요청에 대한 에러처리(폴링에 대한 에러처리 아님)
            $studyListContainer.html(`
                <div class="component base_list_component study_card">
                    <div class="study_info">
                        <div class="component_title">자료 생성 중 오류가 발생하였습니다</div>
                        <div class="study_content">잠시 후에 다시 시도해 주세요</div>
                    </div>
                </div>
            `);
        }
    }

    /**
     * 폴링 요청 메서드 호출하여 DONE/FAILED 처리된 job을 응답받아 
     * 전달 받은 요소에 랜더링
     * <p>
     *     이 함수는 병렬적으로 호출 되어 각각의 폴링 요청에 대한 빠른 처리가 가능
     * @param job 폴링 요청할 job 데이터
     * @param index 학습 카드의 인덱스 (첫 번째 요소인지 확인하기 위해)
     * @param $studyCard 카드 랜더링할 요소
     */
    async function pollStudyJobStatus(job, index, $studyCard) {
        console.log(`pollStudyJobStatus() 실행 job(${job.jobId}) status: ${job.status}`);

        try {
            // 폴링 요청 시작
            const url = `/api/studies/status/${job.jobId}`; // 폴링 요청 url

            // 폴링의 상태가 PROCESSING이 아닐 경우 반환 됨 (진행상태 표시하지 않음)
            const pollJob = await pollJobStatus(url, $studyCard, isUseSpinner);

            // 반환된 job에 대한 랜더링 (랜더링 함수 내에서 DONE/FAILED 처리 됨)
            renderStudyCard(pollJob, index, $studyCard); // job 객체, 첫 번째 요소 확인 값, 랜더링 요소
        } catch (error) {
            // 폴링에 대한 에러처리 (타임아웃 포함)
            $studyCard.html(`
                <div class="study_info">
                    <div class="component_title">잠시 후에 다시 시도해 주세요</div>
                    <div class="study_content">${error.message}</div>
                </div>
            `);
        }
    }

    // 학습 자료 목록이 추가될 부모 요소가 있을 경우 실행
    if ($studyListContainer.length) {
        await renderFragmentDailyStudies(); // 실행
    }
})