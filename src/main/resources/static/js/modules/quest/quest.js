import {
    apiGetDailyQuestList,
    apiPutDailyQuestUserObtainReward
} from "./quest-api.js";

$(document).ready(async function () {
    // 일일도전과제 컴포넌트 관련
    const $questContainer = $("#quest-container"); // 일일 도전과제 컴포넌트 부모 요소
    const isLoggedIn = $questContainer.data("logged-in"); // 인증 상태
    const $questListContainer = $(".quest_list_container");
    const $qusetTotalCount = $("#total-count"); // 일일 도전과제 총 개수 요소
    const $qusetObtainedCount = $("#obtained-count"); // 보상 수령건 요소
    const $batchObtainBtn = $("#batch-obtain-btn"); // 일괄 획득 버튼
    const $batchObtainBtnInfo = $("#batch-obtain-btn-info"); // 일괄 획득 버튼 부모 요소
    let totalCount = 0; // 일일 도전과제 개수
    let obtainedCount = Number($questListContainer.data("obtained-count")) || 0; // 보상 수령 개수
    let successCount = Number($questListContainer.data("success-count")) || 0; // 완료된 도전과제 개수

    // 사용자에 대한 모든 퀘스트와 진행도 및 보상 획득 여부
    // 정보를 포함한 목록 조회 요청하여 리스트 추가
    async function setDailyQuestList() {
        console.log("setDailyQuestList() 실행");

        // 모든 퀘스트와 진행도 및 보상 획득여부 목록 조회 요청
        // 미인증 시 기본 모든 퀘스트 정보만 요청
        try {
            const dailyQuestList = await apiGetDailyQuestList(isLoggedIn);
            totalCount = dailyQuestList.length; // 일일 도전과제 개수 취합

            let content = "";
            for (let quest of dailyQuestList) {
                const isObtained = quest.obtained; // 보상 수령 여부
                const isSucceed = quest.succeed; // 퀘스트 완료 여부
                if (isObtained) obtainedCount++; // 보상 수령 개수 취합
                if (isSucceed) successCount++; // 퀘스트 완료 개수 취합

                content += !isObtained
                    ? `<div class="component_info small with_icon">
                        <div class="list_info">
                            <p>${quest.contentText}</p>
                            <div class="bar_gauge">
                                <div class="gauge" style="width: ${quest.scorePercent || 0}"></div>
                                <span class="gauge_info">${quest.currentValue || 0} / ${quest.totalScore}</span>
                            </div>
                        </div>
                            
                        ${isSucceed // 퀘스트 완료
                        ? `
                            <input type="hidden" name="dailyQuestUserNo" value="${quest.dailyQuestUserNo}"/>
                            <button type="button" 
                                    data-daily-quest-user-no="${quest.dailyQuestUserNo}" 
                                    class="btn small white get_hint_btn">
                                    <img src="/images/favicon-32x32.png" alt="get hints">
                            </button>
                        `
                        : `<a href="${quest.dailyQuestLink}" class="btn small white">이동</a>`
                    }
                    </div>`
                    : "";
            }

            if (obtainedCount === totalCount) {
                // 보상 수령건이 모든 퀘스트 수와 같다면 모든 퀘스트 완료+보상수령으로 판단
                $questContainer.remove();
            } else {
                // 퀘스트리스트 추가
                $questListContainer.html(content);
                $qusetTotalCount.text(totalCount); // 일일 도전과제 총 개수 입력
                $qusetObtainedCount.text(obtainedCount); // 보상 수령 개수 입력
            }

            // 퀘스트 완료 개수와 보상 획득 수가 같다면 일괄 획득 버튼 비활성화
            if (successCount === 0 || successCount === obtainedCount) {
                $batchObtainBtnInfo.html(`
                    <span class="btn tiny primary disabled">일괄 획득</span>
                `);
            } else if (successCount > obtainedCount) {
                $batchObtainBtnInfo.html(`
                    <button type="submit" id="batch-obtain-btn" class="btn tiny primary">일괄 획득</button>
                `);
            }
        } catch (error) {
            console.error("일일 도전과제 리스트 요청 실패: ", error.message);
            $questListContainer.html(
                `<div class="component_info small with_icon">
                <div class="list_info">
                    <p class="list_info">일일 도전과제 목록을 가져오는데 실패하였습니다. 다시 시도해 주세요.</p>
                </div>
            </div>`
            );
        }
    }

    // 일일 도전과제 컴포넌트 fragment로 사용되는 곳에서만 수행
    if ($questContainer.length) {
        await setDailyQuestList(); // 실행
    }

    // 일일 도전과제 보상 관련
    const $getHintBtn = $(".get_hint_btn"); // 힌트 획득 버튼

    // 힌트 획득 처리 및 획득 후 힌트 개수 요청 버튼 이벤트
    $getHintBtn.click(async function () {
        console.info("일일 도전과제 보상 획득 이벤트 실행");

        const $this = $(this);
        const dailyQuestUserNo = $this.data("daily-quest-user-no");
        
        $this.addClass("disabled"); // 중복 클릭 방지

        try {
            const response = await apiPutDailyQuestUserObtainReward(dailyQuestUserNo);
            console.log("apiPutDailyQuestUserObtainReward() 실행 결과: ", response);

            if (response) {
                obtainedCount++; // 보상 수령 카운트 증가
                $qusetObtainedCount.text(obtainedCount); // (보상 획득 수/퀘스트 개수) 표시 업데이트
                console.log("successCount: ", successCount);
                console.log("obtainedCount: ", obtainedCount);

                if ($questContainer.length) {
                    // 일일 도전과제 컴포넌트 fragment로 사용되는 곳에서만 수행
                    $this.closest(".component_info").remove(); // 퀘스트 리스트 제거
                    getHintCountAction(response); // 프로필 힌트 개수 업데이트

                    if (obtainedCount === totalCount) {
                        // 보상 수령건이 모든 퀘스트 수와 같다면 모든 퀘스트 완료+보상수령으로 판단
                        $questContainer.remove();
                    }
                } else {
                    // 일일 도전과제 페이지일 경우
                    // 정상 응답 시 해당 퀘스트 리스트 비활성화
                    $this.closest(".component_info").addClass("disabled");
                }

                // 퀘스트 완료 개수와 보상 획득 수가 같다면 일괄 획득 버튼 비활성화
                if (successCount === 0 || successCount === obtainedCount) {
                    $batchObtainBtn.remove(); // 현재 활성화된 일괄 획득 버튼(submit) 및 id 제거
                    $batchObtainBtnInfo.html(`
                        <span class="btn primary disabled">일괄 획득</span>
                    `);
                }
            }
        } catch (error) {
            console.error(error);
            alert("보상 획득에 실패했습니다. 다시 시도해 주세요.");
            location.reload();
        }
    });
    
    // 프로필 힌트 개수 증가 액션
    const $profileHintCount = $("#hint-count"); // 프로필 힌트 개수 표시 요소
    function getHintCountAction(hintCount) {
        // 일일 도전과제 컴포넌트 fragment로 사용되는 곳에서만 수행
        if (!$profileHintCount.length) return;

        // 액션 효과 추가
        $profileHintCount.text(hintCount).addClass("action");

        // 일정 시간 지난 후 제거
        setTimeout(() => $profileHintCount.removeClass("action"), 200);
    }

});