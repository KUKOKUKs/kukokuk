import {pollJobStatus} from "../../utils/poll-job-utils.js";
import {renderStudyCard, renderStudyListSkeleton} from "./study-renderer.js";
import {apiGetDailyStudies} from "./study-api.js";

/**
 * 학습 목록 프레그먼츠에 추가할 학습 자료 요청(학습 자료 개수: 컨트롤러 기본 값 5)
 * 최초 요청 후 폴링하며 진행 상태 표시
 * 요청시 기본 5개로 요청이 되며 usableRows 만큼만 활용 됨
 * 예외사항으로는 usableRows가 컨트롤러 기본 값 5보다 클 경우엔 usableRows만큼 요청
 * !! 사용할 데이터 개수 외로 미리 생성되도록 하여 사용자 경험 향상 !!
 * @param usableRows 사용할 데이터 개수
 * @param requestRows 요청할 데이터 개수
 * @param $studyListContainer 목록이 추가될 부모 요소
 * @param isUseSpinner 폴링 진행 상태(진행률/메시지)를 추가/업데이트 사용 여부
 */
export async function renderFragmentDailyStudies(usableRows, requestRows, $studyListContainer, isUseSpinner) {
    console.log("renderFragmentDailyStudies() 실행");

    try {
        // 최초 요청에 대한 응답
        // List<JobStatusResponse<DailyStudySummaryResponse>>> 객체
        const jobStatusList = await apiGetDailyStudies(requestRows);

        // 실제 활용할 데이터만 추출 (앞에서부터 studyRows개)
        const usableJobStatusList = jobStatusList.slice(0, usableRows);
        console.log("사용할 데이터만 추출 usableJobStatusList: ", usableJobStatusList);
        $studyListContainer.html(renderStudyListSkeleton(usableJobStatusList, isUseSpinner)); // 스켈레톤 로딩 세팅 (data-job-id 속성 추가)

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
                // 폴링 요청할 경로
                const pollUrl = `/api/studies/status/${job.jobId}`;
                void pollStudyJobStatus(job, index, pollUrl, $studyCard, isUseSpinner);
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
 * <p>
 *     jobId 생성 지연 또는 네트워크 오류 시 최대 3회까지 재시도
 * <p>
 *     이후 폴링으로 지속적 확인 요청
 * @param job 폴링 요청할 job 데이터
 * @param index 학습 카드의 인덱스 (첫 번째 요소인지 확인하기 위해)
 * @param pollUrl 폴링 요청할 경로
 * @param $studyCard 카드 랜더링할 요소
 * @param isUseSpinner 폴링 진행 상태(진행률/메시지)를 추가/업데이트 사용 여부
 */
async function pollStudyJobStatus(job, index, pollUrl, $studyCard, isUseSpinner) {
    console.log(`pollStudyJobStatus() 실행 job(${job.jobId}) status: ${job.status}`);

    // 최초 딜레이
    // jobId 생성 되기 전이라면 에러 발생하여 약간의 지연 후 폴링 시작
    const delayMs = 2000 + index * 300; // 인덱스별 약간의 간격 주기
    await new Promise(resolve => setTimeout(resolve, delayMs)); // 지연 대기

    /**
     * 내부 재시도용 헬퍼 함수
     * @param {number} attempt 현재 시도 횟수
     */
    async function tryPoll(attempt = 1) {
        try {
            console.log(`pollStudyJobStatus() job(${job.jobId}) 시도 ${attempt}회차`);

            // 실제 폴링 요청
            // 폴링의 상태가 PROCESSING이 아닐 경우 반환 됨 (진행상태 표시하지 않음)
            const pollJob = await pollJobStatus(pollUrl, $studyCard, isUseSpinner);

            // 반환된 job에 대한 랜더링 (랜더링 함수 내에서 DONE/FAILED 처리 됨)
            renderStudyCard(pollJob, index, $studyCard); // job 객체, 첫 번째 요소 확인 값, 랜더링 요소
        } catch (error) {
            console.warn(`pollStudyJobStatus() 오류 job(${job.jobId}) (${attempt}회차): ${error.message}`);

            if (attempt < 10) {
                // 재시도 가능 (최대 3회)
                const retryDelay = 500 * attempt; // 시도 횟수별로 점진적 지연
                console.log(`${retryDelay}ms 후 재시도 예정...`);
                await new Promise(resolve => setTimeout(resolve, retryDelay));
                return tryPoll(attempt + 1); // 재귀 재호출
            } else {
                // 3회 시도 실패 후 에러 표시
                console.error(`${attempt}회 job(${job.jobId}) 재시도 실패. 폴링 요청 시도 중단`);
                $studyCard.html(`
                    <div class="study_info">
                        <div class="component_title">잠시 후에 다시 시도해 주세요</div>
                        <div class="study_content">${error.message}</div>
                    </div>
                `);
            }
        }
    }

    // 재시도 로직 실행 시작
    await tryPoll();
}

export async function pollTeacherStudyJobStatus(pollUrl, $uploadElement) {
    const jobId = $uploadElement.attr("data-job-id");
    console.log(`pollTeacherStudyJobStatus() 실행 jobId: ${jobId}, pollUrl: ${pollUrl}`);

    /**
     * 내부 재시도용 헬퍼 함수
     * @param {number} attempt 현재 시도 횟수
     */
    async function tryPoll(attempt = 1) {
        try {
            console.log(`pollTeacherStudyJobStatus() jobId(${jobId}) 시도 ${attempt}회차`);

            // 실제 폴링 요청
            // 폴링의 상태가 PROCESSING이 아닐 경우 반환 됨 (진행상태 표시하지 않음)
            const pollJob = await pollJobStatus(pollUrl, null, false);
            console.log("pollJobStatus() pollJob: ", pollJob);

            // 요소 업데이트
            const isDone = pollJob.status === "DONE";
            updateProcessing(isDone, pollJob.status, $uploadElement);

            // 정상 종료 시(DONE) 학습 토글 버튼으로 학습 리스트 확인할 수 있도록 새로고침
            // 현재 진행 중(data-stats='PROCESSING')인 jobID 적용 요소가 없을 경우 모든 병렬 폴링 작업이 끝났다고 판단
            const $parentElement = $(".uploading_list_container"); // 부모요소
            const isProcessing = $parentElement.find(".upload_list").is("[data-status='PROCESSING']"); // 아직 진행 중
            const isFailed = $parentElement.find(".upload_list").is("[data-status='ERROR'], [data-status='FAILED']"); // 실패한 요청이 있음
            console.log("isProcessing element check: ", $parentElement.find(".upload_list"));
            console.log("isProcessing element check: ", $parentElement.find(".upload_list"));

            if (!isProcessing) { // 모든 병렬 폴링 호출이 종료가 되었을 경우
                if (!isFailed) {
                    // 모든 요청이 성공일 경우
                    await new Promise(resolve => setTimeout(resolve, 3000)); // 지연 대기
                    alert("처리가 완료 되었습니다.");
                    location.reload(); // 새로고침
                } else {
                    // 실패한 요청이 한개라도 있을 경우
                    alert("생성에 실패한 요청이 있습니다.\n파일을 확인하여 새로고침 후 다시 시도해 주세요.");
                }
            }
            
        } catch (error) {
            console.warn(`pollStudyJobStatus() 오류 jobId(${jobId}) (${attempt}회차): ${error.message}`);

            if (attempt < 4) {
                // 재시도 가능 (최대 3회)
                const retryDelay = 500 * attempt; // 시도 횟수별로 점진적 지연
                console.log(`${retryDelay}ms 후 재시도 예정...`);
                await new Promise(resolve => setTimeout(resolve, retryDelay));
                return tryPoll(attempt + 1); // 재귀 재호출
            } else {
                // 3회 시도 실패 후 에러 표시
                console.error(`${attempt}회 jobId(${jobId}) 재시도 실패. 폴링 요청 시도 중단`);
                alert(`${$uploadElement.find(".info").text()}\n학습 자료 업로드에 실패하였습니다.\n나중에 다시 시도해 주세요.`);
                updateProcessing(false, 'ERROR', $uploadElement)
            }
        }
    }

    // 재시도 로직 실행 시작
    await tryPoll();
}

function updateProcessing(isDone, status, $uploadElement) {
    $uploadElement.attr("data-status", status);
    $uploadElement.css("--percent", isDone ? "100%" : "0%"); // 통신완료
    $uploadElement.removeClass("loading_spinner"); // 로딩 제거
    $uploadElement.addClass("sub_font"); // 사이즈 맞추기
    if (!isDone) $uploadElement.addClass("color_gray"); // FAILED이거나 오류일 경우 텍스트 색상 변경

    // 체크/엑스 로 변경
    $uploadElement.prepend(`
        <iconify-icon class="icon sub_font" 
            icon="${isDone ? 'streamline-sharp-color:check-flat' : 'fxemoji:crossmark'}"></iconify-icon>
    `);
}