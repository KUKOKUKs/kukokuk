import {apiGetDailyQuestList} from "./quest-api.js";
import {replaceQuestLinkByContentType} from "../../utils/handler-util.js";

$(document).ready(async function () {
    // 일일도전과제 컴포넌트 관련
    const isLoggedIn = $("#quest-container").data("logged-in");
    const $questListContainer = $(".quest_list_container");

    // 사용자에 대한 모든 퀘스트와 진행도 및 보상 획득여부
    // 정보를 포함한 목록 조회 요청하여 리스트 추가
    async function setDailyQuestList() {
        console.log("setDailyQuestList() 실행");

        // 모든 퀘스트와 진행도 및 보상 획득여부 목록 조회 요청
        // 미인증 시 기본 모든 퀘스트 정보만 요청
        const dailyQuestList = await apiGetDailyQuestList(isLoggedIn);

        let constent = "";
        for (let quest of dailyQuestList) {
            const isExpType = quest.expType;
            const totalScore = !isExpType ? quest.count : quest.point;
            const progressValue = quest.progressValue || 0;
            const currentValue = progressValue >= totalScore ? totalScore : progressValue;
            const scorePercent = ((progressValue / totalScore) * 100).toFixed(1) + "%";
            const isSucceed = progressValue >= totalScore;
            const isObtained = quest.isObtained != null && quest.isObtained == "Y";

            constent += `
                <div class="component_info small with_icon">
                    <div class="list_info">
                        <p class="list_info">${quest.contentText}</p>
                        <div class="bar_gauge">
                            <div class="gauge" style="width: ${scorePercent}"></div>
                            <span class="gauge_info">${currentValue} / ${totalScore}</span>
                        </div>
                    </div>
                    
                    ${isSucceed // 퀘스트 완료
                        ? isObtained // 보상 수령 완료
                            ? `<span class="btn small white disabled"><img src="/images/favicon-32x32.png" alt="hint obtained"></span>`
                            : `<button type="button" data-daily-quest-user-no="${quest.dailyQuestUserNo}" class="btn small white get_hint_btn">
                                    <img src="/images/favicon-32x32.png" alt="get hints">
                                </button>`
                        : `<a href="${replaceQuestLinkByContentType(quest.contentType)}" class="btn small white">이동</a>`
                    }
                </div>
            `;
        }

        // 퀘스트리스트 추가
        $questListContainer.html(constent);
    }
    await setDailyQuestList();
});