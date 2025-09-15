import {apiGetGroupsAndPagination} from "./group-api.js";

$(document).ready(function () {
    // 그룹 검색 관련
    const $searchComponent = $("#search-component"); // 검색 컴포넌트
    const $groupSearchForm = $("#group-search-form"); // 그룹 검색 폼
    const $groupSearchInput = $groupSearchForm.find(".search_input"); // 검색 인풋

    // 폼 제출 이벤트 발생 시 유효성 검사 후 비동기로 요청
    $groupSearchForm.submit(async function (e) {
        e.preventDefault();
        console.log("그룹 검색 제출 이벤트 발생");

        const keyword = $groupSearchInput.val().trim();

        if (keyword === "") {
            alert("검색어를 입력해 주세요.");
            $groupSearchInput.focus();
            return false;
        }

        // 추후 작업 예정
        try {
            // 검색어로 그룹 검색 비동기 요청
            const searchGroups = await apiGetGroupsAndPagination(keyword);

        } catch (error) {
            
        }

    });

});