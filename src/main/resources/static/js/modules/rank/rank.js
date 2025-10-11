import {apiGetRanks} from "./rank-api.js";
import {formatDateToMonth} from "./rank-util.js";
import {setRankList} from "./rank-handler.js";

$(document).ready(function () {
    // 랭크 관련
    const $rankContainer = $("#rank-container"); // 랭크 컨텐츠 컨테이너 요소
    const $rankMonthBtn = $("#rank-month-btn .step_info"); // 월별 랭크 스테퍼 버튼 요소(prev, next)
    const regexRankMonth = /^\d{4}-(0[1-9]|1[0-2])$/; // yyyy-MM 검증 정규식

    // 랭크 월 스테퍼 버튼 클릭 이벤트 핸들러
    $rankMonthBtn.click(async function () {
        console.log("랭크 월 스테퍼 버튼 클릭");

        const $this = $(this); // 클릭한 요소 prev 또는 next
        const $parentElement = $this.closest("#rank-month-btn"); // 부모 요소
        const $info = $parentElement.find(".info"); // 현재 적용된 월 표시 요소

        // 중복 클릭 방지로 이전, 다음 버튼 비활성화
        $parentElement.find(".prev, .next").addClass("disabled");

        let currentRankMonth = $info.text(); // 현재 적용된 월 텍스트
        const now = new Date(); // 오늘 날짜
        const defaultRankMonth = formatDateToMonth(now); // 당월("yyyy-MM")

        // 정규식 불일치 시 당월 사용
        if (!regexRankMonth.test(currentRankMonth)) currentRankMonth = defaultRankMonth;

        // 클릭한 요소에 따라 rankMonth 값 계산하여 적용
        let targetDate = new Date(currentRankMonth + "-01"); // "2025-10-01" 안전하게 Date 객체 생성
        if ($this.hasClass("prev")) {
            // prev 버튼을 클릭했을 경우
            console.log("prev 버튼 클릭");
            targetDate.setMonth(targetDate.getMonth() - 1); // 현재 적용된 월의 이전 달
        } else if ($this.hasClass("next")) {
            // next 버튼을 클릭했을 경우
            console.log("next 버튼 클릭");
            targetDate.setMonth(targetDate.getMonth() + 1); // 현재 적용된 월의 다음 달
        }

        // 유효 범위 자동 보정
        const currentDate = new Date(defaultRankMonth + "-01"); // 당월
        const threeMonthsAgo = new Date(now.getFullYear(), now.getMonth() - 2); // 최근 3개월 시작
        let fixedMonth;

        if (targetDate > currentDate) {
            // 미래 월일 경우 당월로 적용
            fixedMonth = defaultRankMonth;
        } else if (targetDate < threeMonthsAgo) {
            // 최근 3개월 이전일 경우 최근 3개월 시작 월로 적용
            fixedMonth = formatDateToMonth(threeMonthsAgo);
        } else {
            // 정상 범위일 경우 유지
            fixedMonth = formatDateToMonth(targetDate);
        }
        
        // 랭크 목록 비동기 요청
        try {
            const ranksResponseDtos = await apiGetRanks(fixedMonth); // json 으로 변환된 Map 객체

            // 요청 성공 시 처리(미리 처리하지 않음)
            $info.text(fixedMonth); // 조회 요청된 월 적용
            
            // prev 버튼 활성화
            let prevDate = new Date(fixedMonth + "-01");
            prevDate.setMonth(prevDate.getMonth() - 1); // 이전 달

            if (prevDate >= threeMonthsAgo) {
                // 유효 범위일 경우 활성화
                $parentElement.find(".prev").removeClass("disabled");
            }

            // next 버튼 활성화
            let nextDate = new Date(fixedMonth + "-01");
            nextDate.setMonth(nextDate.getMonth() + 1); // 다음 달

            if (nextDate <= currentDate) {
                // 유효 범위일 경우 활성화
                $parentElement.find(".next").removeClass("disabled");
            }
            
            // 랭크 목록 세팅
            setRankList(ranksResponseDtos, $rankContainer);

        } catch (error) {
            console.error("순위 정보 요청 실패: ", error.message);
            alert("순위 목록을 가져오는데 실패하였습니다.\n다시 시도해 주세요.");
            location.reload(); // 새로 고침
        }
    });

});