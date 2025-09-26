import {apiGetGroupsAndPagination} from "./group-api.js";
import {setGroupList} from "./group-handler.js";
import {setPagination} from "../../utils/handler-util.js";

$(document).ready(function () {
    // 그룹 검색 관련
    const $searchComponent = $("#group-search-component"); // 검색 컴포넌트
    const $searchFormWrap = $searchComponent.find(".search_form_wrap"); // 검색 폼 부모 요소
    const $groupSearchForm = $("#group-search-form"); // 그룹 검색 폼
    const $groupSearchInput = $groupSearchForm.find(".search_input"); // 검색 인풋
    const $groupSearchListComponent = $("#group-search-list-component"); // 그룹 목록이 추가될 부모 요소

    // 폼 제출 이벤트 발생 시 비동기 요청
    $groupSearchForm.submit(async function (e) {
        e.preventDefault();
        const keyword = $groupSearchInput.val().trim();
        console.log("그룹 검색 제출 이벤트 실행 keyword: ", keyword);

        // 랜덤 그룹 목록 리셋 버튼
        // 제이쿼리 객체 참조로 remove가 되어도 삭제된 노드 객체가 남아 있기 때문에
        // 함수 이벤트 발생 시 마다 새로 선택
        const $groupResetBtn = $searchFormWrap.find("#reset-btn");
        $groupResetBtn.addClass("disabled"); // 리셋 이벤트 중복 발생 방지

        try {
            // 검색어로 그룹 검색 비동기 요청
            const groupsAndPagination = await apiGetGroupsAndPagination(keyword);

            // 새로고침 시에도 적용되도록 url 변경(뒤로가기 적용 안됨)
            const params = new URLSearchParams();

            if (keyword && keyword !== "") {
                params.set("keyword", keyword);
                params.set("page", "1"); // 폼 제출 시에는 항상 1페이지
                $groupResetBtn.remove(); // 새로고침 버튼 제거(랜덤 리스트일 경우에만 적용)
            } else {
                // 그룹 페이지 전용 기능(랜덤 리스트/한 페이지) 요청 버튼(리셋) 추가
                if (!$groupResetBtn.length) {
                    $searchFormWrap.append(`
                        <button type="button"
                                class="icon title_font"
                                id="reset-btn">
                            <iconify-icon class="icon title_font" icon="marketeq:refresh-round"></iconify-icon>
                        </button>
                    `);
                }
            }

            const newUrl = params.toString()
                ? `${location.pathname}?${params.toString()}`
                : location.pathname;
            history.replaceState(null, "", newUrl);

            // 그룹 리스트 세팅
            setGroupList(groupsAndPagination.items, $groupSearchListComponent);
            
            // 페이지네이션 세팅(페이지네이션 세팅 전 url 수정되어 있어야 함)
            // 랜덤 리스트 요청일 경우 페이지네이션 세팅 로직 내에 null 처리 됨으로 페이징 추가 안됨
            setPagination(groupsAndPagination.pagination, $searchComponent);
        } catch (error) {
            console.error("그룹 검색 요청 실패: ", error.message);
            alert("그룹 검색 목록을 가져오는데 실패하였습니다.\n다시 시도해 주세요.");
            location.reload(); // 새로 고침
        } finally {
            $groupResetBtn.removeClass("disabled"); // 버튼 활성화
        }
    });

    // keyword가 없을 경우에만 실행되는 리셋 버튼 이벤트 발생 시 랜덤 그룹 리스트 목록 비동기 요청
    // #reset-btn요소가 비동기 요청으로 인해 동적으로 제거/추가 되는 상황으로
    // 부모 요소에 이벤트 위임하여 처리
    $searchFormWrap.on("click", "#reset-btn", async function () {
        console.log("랜덤 그룹 목록 리셋 버튼 이벤트 실행");

        const $groupResetBtn = $(this); // 실제 클릭된 버튼 (동적으로 새로 생겨도 참조 가능)
        $groupResetBtn.addClass("disabled"); // 다중 클릭 방지

        try {
            // 랜덤 그룹 검색 비동기 요청
            const groupsAndPagination = await apiGetGroupsAndPagination();

            // url 초기화(기본 도메인만 남김)
            history.replaceState(null, "", location.pathname);

            // 그룹 리스트 세팅
            setGroupList(groupsAndPagination.items, $groupSearchListComponent);
            
            // 랜덤 그룹 목록은 한 페이지로 페지이네이션 정보가 없어 페지이네이션 요소 제거
            $searchComponent.find(".pagination").remove();
        } catch (error) {
            console.error("그룹 목록 리셋 요청 실패: ", error.message);
            alert("그룹 목록 리셋 요청 실패하였습니다.\n다시 시도해 주세요.");
            location.reload(); // 새로 고침
        } finally {
            $groupResetBtn.removeClass("disabled"); // 버튼 활성화
        }
    });

    // 페이징 버튼 이벤트 발생 시 비동기 요청
    // 페이지네이션 요소가 비동기 요청으로 인해 동적으로 제거/추가 되는 상황으로
    // 부모 요소에 이벤트 위임하여 처리
    $searchComponent.on("click", ".paging .page_link", async function (e) {
        e.preventDefault();
        console.log("페이징 버튼 이벤트 실행");

        // 클릭한 페이징 버튼의 href 가져오기
        const href = $(this).attr("href"); // href에는 path를 포함하기 때문에 
        const url = new URL(href, location.origin); // 절대 주소화하여 안전하게 처리
        const params = url.searchParams; // 쿼리스트링 가져오기

        // 가져온 href의 값으로 쿼리스트링 파싱
        const keyword = params.get("keyword");
        const page = parseInt(params.get("page") || "1", 10);

        try {
            const groupsAndPagination = await apiGetGroupsAndPagination(keyword, page);

            const newUrl = `${location.pathname}?${params.toString()}`;
            history.replaceState(null, "", newUrl);

            // 그룹 리스트 세팅
            setGroupList(groupsAndPagination.items, $groupSearchListComponent);

            // 페이지네이션 세팅(페이지네이션 세팅 전 url 수정되어 있어야 함)
            setPagination(groupsAndPagination.pagination, $searchComponent);
        } catch (error) {
            console.error("그룹 검색 요청 실패: ", error.message);
            alert("그룹 검색 목록을 가져오는데 실패하였습니다.\n다시 시도해 주세요.");
            location.reload(); // 새로 고침
        }
    });
    
    //  그룹 학습/검색 스위치 토글 버튼 이벤트
    const $groupSearchToggleInfo = $(".group_search_toggle_info"); // 학습/검색 요소들의 부모 요소
    const $groupSwitchToggleBtn = $("#group-switch-toggle-btn"); // 스위치 토글 버튼
    $groupSwitchToggleBtn.click(function () {
        const $this = $(this);

        if ($this.hasClass("on")) {
            $this.removeClass("on");
            $groupSearchToggleInfo.removeClass("search_on");
        } else {
            $this.addClass("on");
            $groupSearchToggleInfo.addClass("search_on");
        }
    });
});