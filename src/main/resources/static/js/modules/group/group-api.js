import {apiErrorProcessByXhr} from "../../utils/api-error-util.js";

/**
 * 그룹 검색 비동기 요청
 * @param keyword 검색어
 * @returns 검색한 그룹 목록 정보, 페이지네이션
 */
export async function apiGetGroupsAndPagination(keyword) {
    console.log("apiGetGroupsAndPagination() api 요청 실행");
    try {
        const response = await $.ajax({
            method: "PUT",
            url: "/api/groups",
            data: {keyword},
            dataType: "json",
        });

        console.log("apiGetGroupsAndPagination() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}