import {apiErrorProcessByXhr} from "../../utils/api-error-util.js";

/**
 * 그룹 검색(페이지네이션) 비동기 요청
 * <p>
 * keyword 유무에 따라 랜덤리스트(기본값)/검색 리스트 전달
 * @param keyword 검색어
 * @param page - 조회할 페이지 번호 (기본값 1)
 * @returns 검색한 그룹 목록 정보, 페이지네이션(랜덤리스트일 경우 null)
 */
export async function apiGetGroupsAndPagination(keyword = null, page = 1) {
    console.log("apiGetGroupsAndPagination() api 요청 실행", {keyword, page});
    try {
        const response = await $.ajax({
            method: "GET",
            url: "/api/groups",
            data: {keyword, page},
            dataType: "json",
        });

        console.log("apiGetGroupsAndPagination() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}