import {apiErrorProcessByXhr} from "../../utils/api-error-util.js";
import {consoleLogForm} from "../../utils/handler-util.js";

/**
 * 그룹 가입 비동기 요청
 * @param groupNo 그룹 번호
 * @param password 비밀번호 설정된 그룹일 경우에만 값 입력
 * @returns 성공 여부
 */
export async function apiPostJoinGroup(groupNo, password = null) {
    console.log("apiPostJoinGroup() api 요청 실행 ", {groupNo, password});
    try {
        const response = await $.ajax({
            method: "POST",
            url: "/api/groups/join",
            data: {groupNo, password},
            dataType: "json",
        });

        console.log("apiPostJoinGroup() api 요청 response: ", response);
        return response;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}

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

/**
 * 그룹 생성 비동기 요청
 * @param createFormElement 생성 폼 요소
 * @returns {Promise<*>} 성공 응답의 data 객체를 반환
 * <p>
 *     예: { isValid: true, groupNo: 123 } 또는 { isValid:false, errors: {...} }
 */
export async function apiPostGroups(createFormElement) {
    console.log("apiPostGroups() api 요청 실행");

    // FormData 생성
    const formData = new FormData(createFormElement);
    consoleLogForm(formData); // 폼 데이타 값 로그

    try {
        const response = await $.ajax({
            method: "POST",
            url: "/api/groups",
            data: formData,
            processData: false,       // FormData를 문자열(직렬화)로 바꾸지 않음
            contentType: false,       // 브라우저가 boundary 포함한 content-type을 설정하게 함
            dataType: "json",
        });

        console.log("apiPostGroups() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}

/**
 * 그룹 수정 비동기 요청
 * @param modifyFormElement 수정 폼 요소
 * @returns {Promise<*>} 성공 응답의 data 객체를 반환
 * <p>
 *     예: { isValid: true, groupNo: 123 } 또는 { isValid:false, errors: {...} }
 */
export async function apiPutGroups(modifyFormElement) {
    console.log("apiPutGroups() api 요청 실행");

    // FormData 생성
    const formData = new FormData(modifyFormElement);
    consoleLogForm(formData); // 폼 데이타 값 로그

    try {
        const response = await $.ajax({
            method: "PUT",
            url: "/api/groups",
            data: formData,
            processData: false,       // FormData를 문자열(직렬화)로 바꾸지 않음
            contentType: false,       // 브라우저가 boundary 포함한 content-type을 설정하게 함
            dataType: "json",
        });

        console.log("apiPutGroups() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}

/**
 * 그룹 삭제 비동기 요청
 * @param $deleteFormElement 삭제 폼 요소
 * @returns {Promise<*>} 성공 응답의 data 객체를 반환
 * <p>
 *     예: { isSuccess: true } 또는 { isSuccess:false, message: "..." }
 */
export async function apiDeleteGroups($deleteFormElement) {
    console.log("apiDeleteGroups() api 요청 실행");

    const groupNo = $deleteFormElement.find("input[name='groupNo']").val();
    const isDeleteCheck = $deleteFormElement.find("input[name='deleteGroup']").is(":checked");

    // 삭제 여부 체크 재검증
    if (!isDeleteCheck) {
        alert("우리반 삭제는 동의가 필요합니다.\n삭제를 원하실 경우 '예'를 선택해 주세요.");
        return;
    }

    try {
        const response = await $.ajax({
            method: "DELETE",
            url: `/api/groups/${groupNo}`,
            data: {isDeleteCheck},
            dataType: "json",
        });

        console.log("apiDeleteGroups() api 요청 response: ", response);
        return response.data;
    } catch (xhr) {
        apiErrorProcessByXhr(xhr.responseJSON);
    }
}