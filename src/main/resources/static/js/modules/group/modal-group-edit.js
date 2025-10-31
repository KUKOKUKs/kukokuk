import {
    validateCreateGroupForm,
    validateDeleteGroupForm,
    validateModifyGroupForm
} from "./group-form-validator.js";
import {setOnlyDigit} from "../../utils/handler-util.js";
import {
    addInputErrorMessage,
    clearInputErrorMessage
} from "../../utils/form-error-util.js";
import {apiDeleteGroups, apiPostGroups, apiPutGroups} from "./group-api.js";

$(document).ready(function () {
    // 모달창 그룹 생성/수정/삭제 관련
    const $createGroupForm = $("#create-group-form"); // 그룹 생성 폼 요소
    const $modifyGroupForm = $("#modify-group-form"); // 그룹 수정 폼 요소
    const $deleteGroupForm = $("#delete-group-form"); // 그룹 삭제 폼 요소

    // 모달창에 있는 폼 활용으로 유효성 검증을 통한 에러 메세지를 노출해야하여 비동기 요청 활용
    // 그룹 생성 폼 제출 이벤트 발생 시 유효성 검사 후 비동기 요청
    $createGroupForm.submit(async function (e) {
        e.preventDefault();
        const $this = $(this);

        // 검증 실패 시 클라이언트 측 메시지를 표시하고 전송 중단
        if (!validateCreateGroupForm($this)) {
            return false;
        }

        const $submitBtn = $this.find("button[type=submit]"); // 제출 버튼
        $submitBtn.addClass("disabled loading_spinner center"); // 로딩 표시 및 중복 클릭 방지

        // 유효성 검사 통과 시 제출
        try {
            const response = await apiPostGroups(this);

            if (!response.isValid) {
                // 유효성 검증 실패 시 에러 처리
                for (const [field, msg] of Object.entries(response.errors)) {
                    // 해당 필드의 에러메세지 추가
                    addInputErrorMessage($this.find(`input[name="${field}"]`), msg);
                }
                return false;
            }

            // 생성 성공 시 해당 그룹 페이지로 이동
            location.href = `/group/teacher?groupNo=${response.groupNo}`;
        } catch (error) {
            alert(error.message);
            location.reload();
        } finally {
            $submitBtn.removeClass("disabled loading_spinner center");
        }
    });

    // 그룹 수정 폼 제출 이벤트 발생 시 유효성 검사 후 비동기 요청
    $modifyGroupForm.submit(async function (e) {
        e.preventDefault();
        const $this = $(this);

        // 검증 실패 시 클라이언트 측 메시지를 표시하고 전송 중단
        if (!validateModifyGroupForm($this)) {
            return false;
        }

        const $submitBtn = $this.find("button[type=submit]"); // 제출 버튼
        $submitBtn.addClass("disabled loading_spinner center"); // 로딩 표시 및 중복 클릭 방지

        // 유효성 검사 통과 시 제출
        try {
            const response = await apiPutGroups(this);

            if (!response.isValid) {
                // 유효성 검증 실패 시 에러 처리
                for (const [field, msg] of Object.entries(response.errors)) {
                    // 해당 필드의 에러메세지 추가
                    addInputErrorMessage($this.find(`input[name="${field}"]`), msg);
                }
                return false;
            }

            // 생성 성공 시 해당 그룹 페이지로 이동
            location.href = `/group/teacher?groupNo=${response.groupNo}`;
        } catch (error) {
            alert(error.message);
            location.reload();
        } finally {
            $submitBtn.removeClass("disabled loading_spinner center");
        }
    });

    // 그룹 삭제 폼 제출 이벤트 발생 시 유효성 검사 후 제출
    $deleteGroupForm.submit(async function (e) {
        e.preventDefault();
        const $this = $(this);

        // 검증 실패 시 클라이언트 측 메시지를 표시하고 전송 중단
        if (!validateDeleteGroupForm($this)) {
            return false;
        }

        const isConfirm = confirm('우리반 삭제는 복구가 불가능합니다.\n정말 우리반을 삭제하시겟습니까?');

        // 취소 시 리턴
        if (!isConfirm) return false;

        const $submitBtn = $this.find("button[type=submit]"); // 제출 버튼
        $submitBtn.addClass("disabled loading_spinner center"); // 로딩 표시 및 중복 클릭 방지

        // 유효성 검사 통과 시 제출
        try {
            const response = await apiDeleteGroups($this);

            if (!response.isSuccess) {
                // 삭제 처리 취소/실패 시
                alert(response.message);
                return false;
            }

            // 삭제 성공 시 그룹 페이지로 이동
            location.href = "/group/teacher";
        } catch (error) {
            alert(error.message);
            location.reload();
        } finally {
            $submitBtn.removeClass("disabled loading_spinner center");
        }
    });

    // 그룹 생성/수정 시 비밀번호 숫자만 입력 가능 이벤트
    const $modalGroupEdit = $("#modal-group-edit"); // 모달창
    const $groupPwdInput = $modalGroupEdit.find("input[name='password'"); // 비밀번호 인풋 요소
    $groupPwdInput.on("input blur", function () {
        setOnlyDigit($(this));
    });

    // 그룹명 유효성 검증 처리 이벤트
    const $groupTitleInput = $modalGroupEdit.find("input[name='title'"); // 그룹명 인풋 요소
    $groupTitleInput.on("input blur", function () {
        const $this = $(this);

        clearInputErrorMessage($this); // 에러 메세지 초기화
        const val = $this.val();

        if (val === "") {
            addInputErrorMessage($this, "우리반 이름을 입력해 주세요");
        }
    });
});