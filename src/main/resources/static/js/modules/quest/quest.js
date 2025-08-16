import {
    apiGetDailyQuestList,
    apiPutDailyQuestObtainReward
} from "./quest-api.js";
import {replaceQuestLinkByContentType} from "../../utils/handler-util.js";

$(document).ready(async function () {
    // 일일도전과제 컴포넌트 관련
    const $questContainer = $("#quest-container"); // 일일 도전과제 컴포넌트 요소
    const isLoggedIn = $questContainer.data("logged-in"); // 인증 상태
    const $questListContainer = $(".quest_list_container");
    const $qusetSuccessText = $("#success-text"); // 모든 일일 도전과제 완료 시 문구 노출 요소
    const $qusetTotalCount = $("#total-count"); // 일일 도전과제 총 개수 요소
    const $qusetSuccessCount = $("#success-count"); // 완료된 일일 도전과제 개수 요소
    let successCount = 0; // 일일 도전과제 완료 개수
    let totalCount = 0; // 일일 도전과제 개수

    // 사용자에 대한 모든 퀘스트와 진행도 및 보상 획득 여부
    // 정보를 포함한 목록 조회 요청하여 리스트 추가
    async function setDailyQuestList() {
        console.log("setDailyQuestList() 실행");

        // 모든 퀘스트와 진행도 및 보상 획득여부 목록 조회 요청
        // 미인증 시 기본 모든 퀘스트 정보만 요청
        try {
            const dailyQuestList = await apiGetDailyQuestList(isLoggedIn);
            totalCount = dailyQuestList.length; // 일일 도전과제 개수 취합

            let constent = "";
            for (let quest of dailyQuestList) {
                const isExpType = quest.expType; // 퀘스트 타입(경험치, 횟수)
                const totalScore = !isExpType ? quest.count : quest.point; // 퀘스트 완료 조건
                const progressValue = quest.progressValue || 0; // 현재 진행량
                const currentValue = progressValue >= totalScore ? totalScore : progressValue;
                const scorePercent = ((progressValue / totalScore) * 100).toFixed(1) + "%";
                const isSucceed = progressValue >= totalScore; // 완료된 퀘스트인지 체크
                const isObtained = quest.isObtained != null && quest.isObtained == "Y"; // 보상 수령 여부

                if (isSucceed) successCount++; // 완료된 퀘스트 개수 취합

                constent += !isObtained
                    ? `<div class="component_info small with_icon">
                            <div class="list_info">
                                <p class="list_info">${quest.contentText}</p>
                                <div class="bar_gauge">
                                    <div class="gauge" style="width: ${scorePercent}"></div>
                                    <span class="gauge_info">${currentValue} / ${totalScore}</span>
                                </div>
                            </div>
                                
                            ${isSucceed // 퀘스트 완료
                                ? `<button type="button" 
                                            data-daily-quest-user-no="${quest.dailyQuestUserNo}" 
                                            class="btn small white get_hint_btn">
                                            <img src="/images/favicon-32x32.png" alt="get hints">
                                        </button>`
                                : `<a href="${replaceQuestLinkByContentType(quest.contentType)}" class="btn small white">이동</a>`
                            }
                        </div>`
                    : "";


                $qusetTotalCount.text(dailyQuestList.length); // 일일 도전과제 총 개수 입력
                $qusetSuccessCount.text(successCount); // 완료된 도전과제 개수 입력

                if (successCount >= totalCount) { // 모든 도전과제 완료 시 표시
                    $qusetSuccessText.text("완료");
                    $questContainer.find(".component_title").addClass("pd_0");
                }
            }

            $questListContainer.html(constent); // 퀘스트리스트 추가
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
    await setDailyQuestList(); // 실행

    // 일일 도전과제 보상 관련
    const $profileHintCount = $("#hint-count"); // 프로필 힌트 개수 표시 요소
    const $getHintBtn = $(".get_hint_btn"); // 힌트 획득 버튼

    // 힌트 획득 처리 및 획득 후 힌트 개수 요청 버튼 이벤트
    $getHintBtn.click(async function () {
        console.info("일일 도전과제 보상 획득 이벤트 실행");

        const $this = $(this);
        const dailyQuestUserNo = $this.data("daily-quest-user-no");
        
        $this.addClass("disabled"); // 중복 클릭 방지

        try {
            const response = await apiPutDailyQuestObtainReward(dailyQuestUserNo);
            console.log("apiPutDailyQuestObtainReward() 실행 결과: ", response);

            if (response) {
                // 정상 응답 시 해당 퀘스트 리스트 제거 및 프로필 힌트 개수 업데이트
                $this.closest(".component_info").remove();
                getHintCountAction(response);
                successCount++;

                if (successCount >= totalCount) {
                    $qusetSuccessText.text("완료");
                    $questContainer.find(".component_title").addClass("pd_0");
                }
            }
        } catch (error) {
            console.error(error);
            alert("보상 획득에 실패했습니다. 다시 시도해 주세요.");
            location.reload();
        }
    });
    
    // 프로필 힌트 개수 증가 액션
    function getHintCountAction(hintCount) {
        // 액션 효과 추가
        $profileHintCount.text(hintCount).addClass("action");

        // 일정 시간 지난 후 제거
        setTimeout(() => $profileHintCount.removeClass("action"), 200);
    }
    
});