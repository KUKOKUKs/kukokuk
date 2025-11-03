import {
    apiGetGroupsAndPagination,
    apiPostJoinGroup,
    apiPostUploadGroupMaterials
} from "./group-api.js";
import {setGroupList} from "./group-handler.js";
import {
    setPagination,
    setStudyDifficultyList
} from "../../utils/handler-util.js";
import {validateJoinGroupForm} from "./group-form-validator.js";
import {
    addInputErrorMessage,
    clearInputErrorMessage
} from "../../utils/form-error-util.js";
import {validateStudyFiles} from "../../utils/validation-util.js";
import {pollTeacherStudyJobStatus} from "../study/study-poll.js";

$(document).ready(function () {
    // 교사 권한 페이지 관련
    const $teacherStudyUploadContainer = $(".teacher_study_upload_container"); // 교사 학습 자료 생성 컨텐츠 컨테이너 요소
    const $modalGroupStudyBtn = $teacherStudyUploadContainer.find("#modal-group-study-btn"); // 그룹 학습 자료 등록 모달창 열기 버튼
    const $modalGroupStudyCreate = $("#modal-group-study-create"); // 그룹 학습 자료 등록 모달창
    const $teacherStudyCreateForm = $modalGroupStudyCreate.find("#teacher-study-create-form"); // 그룹 학습 자료 등록 폼 요소
    const $modalGroupStudyCreateDifficultyInfoElement = $teacherStudyCreateForm.find("#study-difficulty"); // 단계별 설명 리스트가 추가될 부모 요소
    const $studyDifficultySelect = $teacherStudyCreateForm.find("select[name='difficulty']"); // 학습 단계 선택 요소
    const $teacherStudyFile = $teacherStudyCreateForm.find("#teacher-study-file"); // AI 재구성할 파일 첨부 input
    const $teacherStudyAddBtn = $teacherStudyCreateForm.find("#teacher-study-add-btn"); // 그룹 학습 자료 파일 선택 input 열기 버튼

    // 그룹 학습 자료 파일 선택 버튼 이벤트 핸들러
    $teacherStudyAddBtn.click(function () {
        const studyDifficulty = $studyDifficultySelect.val(); // 학습 단계 선택 값

        console.log("학습 자료 선택 열기 버튼 이벤트 실행 studyDifficulty: ", studyDifficulty);
        
        if (!studyDifficulty) { // 학습 단계 미선택 시
            alert("학습 단계를 선택해 주세요!");
            return false;
        }

        // 학습 단계 선택 완료 후 파일 선택 창 열기
        $teacherStudyFile.click(); // 그룹 학습 자료 파일 선택 인풋 실행
    });

    // 파일 인풋 상태 변환 이벤트 핸들러
    $teacherStudyFile.on("change", async function () {
        console.log("학습 자료 선택");

        const files = this.files;
        if (!files.length) return false;

        const maxFileCount = 3; // 최대 선택 가능 파일 수
        if (files.length > maxFileCount) { // 최대 개수를 초과하였을 경우
            alert(`최대 ${maxFileCount}개의 파일만 선택할 수 있습니다.`);
            this.value = ""; // 입력 초기화
            return false;
        }

        // 유효성 검사
        if (!validateStudyFiles(this)) return false;

        const isConfirm = confirm("선택한 학습 자료로 AI 맞춤 학습을 생성하시겠습니까?");
        if (!isConfirm) {
            this.value = ""; // 선택 초기화
            return false;
        }

        $modalGroupStudyBtn.addClass("disabeld"); // 모달창 열기 버튼 비활성화

        const groupNo = $teacherStudyCreateForm.find("input[name='groupNo']").val();

        try {
            // Map<String, String> {jobId, filename} 반환 받음
            const jobIdList = await apiPostUploadGroupMaterials($teacherStudyCreateForm[0]);

            // 업로드 진행 표시할 요소
            let $uploadingListElement = $teacherStudyUploadContainer.find(".uploading_list_container");

            if (!$uploadingListElement.length) {
                // 업로드 진행 표시할 요소가 없을 경우 생성
                $uploadingListElement = $("<div class='tiny_list_component uploading_list_container sub_font'>");
                $teacherStudyUploadContainer.append($uploadingListElement);
            } else {
                // 업로드 진행 표시할 요소가 있을 경우 현재 표시되어 있는 상태가 PROCESSING가 아닌 상태 요소들 제거
                $teacherStudyUploadContainer.find(".upload_list").not("[data-stats='PROCESSING']").remove();
            }

            // 모달 창 닫기
            console.log("모달창 닫기");
            const $modalAll = $(".modal_wrap");
            // 모달 내부 폼 요소 초기화
            $modalAll.find("form").each(function () {
                $(this).find("button[type='submit']").addClass("disabled");
                this.reset();
            });
            // 모달창 닫기
            $modalAll.hide().removeClass("open");

            let pollSetDataList = new Map();

            // 로딩 요소, 폴링 경로 생성 및 맵에 추가
            Object.entries(jobIdList).forEach(([jobId, filename]) => {
                console.log(`jobId: ${jobId}, filename: ${filename}`);

                // 해당하는 파일의 로딩 요소 생성
                const $uploadItem = $(`
                    <div class="upload_list loading_spinner" style="--size: var(--subFontSize)" data-job-id="${jobId}" data-status="PROCESSING">
                        <p class="file_name text_ellipsis info" style="--percent: 0%;">${filename}</p>
                    </div>
                `);

                $uploadingListElement.append($uploadItem); // 추가
                const pollUrl = `/api/teachers/groups/${groupNo}/materials/${jobId}`; // 폴링 경로 생성
                pollSetDataList.set(jobId, {pollUrl, $uploadItem}); // 맵 추가
                console.log("jobId 순환 처리: ", jobId);
            });
            
            // 생성된 폴링 요청 경로와 로딩요소를 전달하여 폴링 시작(각각 병렬로 실행)
            pollSetDataList.forEach(({pollUrl, $uploadItem}, jobId) => {
                console.log("폴링 요청 시작: ", pollUrl);
                pollTeacherStudyJobStatus(pollUrl, $uploadItem);
            });
        } catch (error) {
            // 요청에 대한 에러처리(폴링에 대한 에러처리 아님)
            console.error(error.messsage);
            alert(error.messsage);
            location.reload() // 새로고침
        }
    });

    // 그룹 검색 관련
    const $searchComponent = $("#group-search-component"); // 검색 컴포넌트
    const isJoinedGroup = $searchComponent.attr("data-joined-group") === "true"; // 사용자의 그룹 가입 여부
    const userNo = Number($searchComponent.attr("data-user-no")); // 로그인 사용자 번호
    const $searchFormWrap = $searchComponent.find(".search_form_wrap"); // 검색 폼 부모 요소
    const $groupSearchForm = $("#group-search-form"); // 그룹 검색 폼
    const $groupSearchInput = $groupSearchForm.find(".search_input"); // 검색 인풋
    const $groupSearchListComponent = $("#group-search-list-component"); // 그룹 목록이 추가될 부모 요소

    // 검색 폼 제출 이벤트 발생 시 비동기 요청
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
            setGroupList(groupsAndPagination.items, $groupSearchListComponent, isJoinedGroup, userNo);
            
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
            setGroupList(groupsAndPagination.items, $groupSearchListComponent, isJoinedGroup, userNo);

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
            setGroupList(groupsAndPagination.items, $groupSearchListComponent, isJoinedGroup, userNo);

            // 페이지네이션 세팅(페이지네이션 세팅 전 url 수정되어 있어야 함)
            setPagination(groupsAndPagination.pagination, $searchComponent);
        } catch (error) {
            console.error("그룹 검색 요청 실패: ", error.message);
            alert("그룹 검색 목록을 가져오는데 실패하였습니다.\n다시 시도해 주세요.");
            location.reload(); // 새로 고침
        }
    });

    // 그룹 탈퇴 폼 제출 이벤트 핸들러
    const $groupOutForm = $("#group-out-form"); // 그룹 탈퇴 폼 요소
    $groupOutForm.submit(function () {
        const isConfirm = confirm('정말 우리반을 탈퇴하시겠습니까?');
        // 취소 시 리턴
        if (!isConfirm) return false;
        this.submit(); // 폼 제출
    });
    
    // 그룹 가입 신청 폼 제출 이벤트 핸들러
    // 비밀번호 설정된 그룹일 경우 비밀번호 입력 모달창 열기
    const $groupJoinForm = $("#group-join-form"); // 그룹 가입 신청 폼
    const $modalGroupPwd = $("#modal-group-pwd"); // 모달창
    const $modalGroupJoinForm = $modalGroupPwd.find("#modal-group-join-form"); // 비밀번호 설정된 그룹 가입 폼

    $(document).on("click", ".group_join_btn", async function () {
        const $this = $(this); // 클릭한 버튼 요소
        const isSecret = $this.attr("data-is-secret") === "true"; // 비밀번호 그룹 여부
        const groupNo = $this.attr("data-group-no"); // 선택된 그룹 번호
        const $groupNoInput = $groupJoinForm.find("input:hidden[name='groupNo']"); // 그룹 번호 입력 인풋

        // 비밀번호가 설정된 그룹일 경우
        if (isSecret && $modalGroupPwd.length) {
            // groupNo 인풋 입력/수정
            const $modalPwdInput = $modalGroupPwd.find("#group-password"); // 비밀번호 입력 인풋
            let $modalGroupNoInput = $modalGroupPwd.find("#modal-group-no"); // 비밀번호 그룹 번호 인풋
            
            // 비밀번호 그룹 번호 인풋이 없을 경우 생성
            if (!$modalGroupNoInput.length) {
                $modalGroupNoInput = $(`<input type="hidden" id="modal-group-no" name="groupNo" value="">`); // 생성
                $modalGroupJoinForm.append($modalGroupNoInput); // 추가
            }
            $modalGroupNoInput.val(groupNo); // 값 입력

            // 해당 모달창 요소가 있을 경우 열기
            $modalGroupPwd.show();

            // 제출 버튼 모두 활성화 실시간 비활성화 기능 사용하지 않음
            $modalGroupPwd.find("button[type='submit']").removeClass("disabled");

            // 약간의 딜레이를 주어 show 후 css transition 적용될 수 있도록 함
            setTimeout(() => {
                $modalGroupPwd.addClass("open");
            }, 10);

            $modalPwdInput.focus(); // 비밀번호 입력 인풋 포커스
            return false;
        }

        // 비밀번호 설정된 그룹이 아닐 경우
        $groupNoInput.val(groupNo);
        $groupJoinForm.submit();
    });
    
    // 그룹 비밀번호 입력 모달 창의 그룹 비밀번호 입력 이벤트 핸들러
    // 정규표현식 적용하여 숫자만 입력 가능하고 최대 4자리까지 자름
    $(document).on("input blur", "#group-password", function () {
        const $this = $(this);
        let value = $this.val();
        clearInputErrorMessage($this); // 에러메세지 제거

        // 입력 값 가공하여 대입
        value = value.replace(/[^0-9]/g, "").substring(0, 4);
        $this.val(value);

        if (value.length < 4) {
            addInputErrorMessage($this, "비밀번호를 입력해 주세요");
        }
    });

    // 비밀번호 설정된 그룹 가입 폼 제출 이벤트 핸들러
    $modalGroupJoinForm.submit(async function (e) {
        e.preventDefault();
        const $this = $(this);

        // 유효성 검사 통과 시 그룹 가입 요청
        if (!validateJoinGroupForm($this)) return false;

        const groupNo = $this.find("input[name=groupNo]").val();
        const $password = $this.find("input[name=password]");

        try {
            const response = await apiPostJoinGroup(groupNo, $password.val());

            if (!response.data) {
                // response.data가 false 경우 비밀번호 틀림
                addInputErrorMessage($password, response.message); // 에러 메세지 추가
                $password.val(""); // 비밀번호 초기화
                $password.focus();
            } else {
                // response.data가 false 경우 가입 성공
                alert(response.message); // 성공 메세지
                location.reload(); // 새로고침
            }
        } catch (error) {
            console.error("그룹 가입 요청 실패: ", error.message);
            alert(error.message);
            location.reload(); // 새로 고침
        }
    });

    // 그룹 생성/수정/삭제 모달창 열기(교사권한 사용자)
    const $modalGruopEditBtn = $(".modal_gruop_edit_btn"); // 모달창 열기 버튼
    const $modalGroupEdit = $("#modal-group-edit"); // 모달창
    $modalGruopEditBtn.click(function () {
        if ($modalGroupEdit.length) {
            // 해당 모달창 요소가 있을 경우 열기
            $modalGroupEdit.show();

            // 제출 버튼 모두 활성화 실시간 비활성화 기능 사용하지 않음
            $modalGroupEdit.find("button[type='submit']").removeClass("disabled");

            // 약간의 딜레이를 주어 show 후 css transition 적용될 수 있도록 함
            setTimeout(() => {
                $modalGroupEdit.addClass("open");
            }, 10);
        }
    });

    // 단계 선택 및 파일 선택 모달창 열기
    $modalGroupStudyBtn.click(async function () {
        if ($modalGroupStudyCreate.length) {
            // 해당 모달창 요소가 있을 경우 열기
            $modalGroupStudyCreate.show();

            // 모달창 단계별 설명 리스트 추가
            await setStudyDifficultyList($modalGroupStudyCreateDifficultyInfoElement);

            // 약간의 딜레이를 주어 show 후 css transition 적용될 수 있도록 함
            setTimeout(() => {
                $modalGroupStudyCreate.addClass("open");
            }, 10);
        }
    });

});