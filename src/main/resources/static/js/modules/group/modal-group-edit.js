import {
    validateCreateGroupForm,
    validateModifyGroupForm
} from "./group-form-validator.js";

$(document).ready(function () {
    // 모달창 그룹 생성/수정/삭제 관련
    const $createGroupForm = $("#create-group-form"); // 그룹 생성 폼 요소
    const $modifyGroupForm = $("#modify-group-form"); // 그룹 수정 폼 요소
    const $deleteGroupForm = $("#delete-group-form"); // 그룹 삭제 폼 요소

    // 그룹 생성 폼 제출 이벤트 발생 시 유효성 검사 후 제출
    $createGroupForm.submit(function (e) {
        e.preventDefault();

        // 유효성 검사 통화 시 제출
        if (validateCreateGroupForm($(this))) {
            this.submit();
        }
    });

    // 그룹 수정 폼 제출 이벤트 발생 시 유효성 검사 후 제출
    $modifyGroupForm.submit(function (e) {
        e.preventDefault();

        // 유효성 검사 통화 시 제출
        if (validateModifyGroupForm($(this))) {
            this.submit();
        }
    });

    // 그룹 삭제 폼 제출 이벤트 발생 시 유효성 검사 후 제출
    $modifyGroupForm.submit(function () {
        const isConfirm = confirm('정말 우리반을 삭제하시겟습니까?');

        // 취소 시 리턴
        if (!isConfirm) return false;

        this.submit();
    });
});