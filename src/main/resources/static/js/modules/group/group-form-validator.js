// 프로필 수정 폼 제출 시 유효성 검사
import {
    addInputErrorMessage,
    allClearFormErrorMessage
} from "../../utils/form-error-util.js";

// 비밀번호 설정된 그룹 가입 폼 유효성 검사
export function validateJoinGroupForm($form) {
    let isValid = true;
    const $groupNo = $form.find("input[name=groupNo]");
    const $password = $form.find("input[name=password]");

    // 폼 인풋 타이틀 및 에러 메세지 초기화
    allClearFormErrorMessage($form);

    // 유효성 검사
    if ($groupNo.val() === "") {
        alert("잘못된 요청입니다.\n다시 시도해 주세요.");
        location.reload(); // 새로고침
        isValid = false;
    }

    let password = $password.val().trim();
    if (!password || password === "" || password.length < 4) {
        addInputErrorMessage($password, "비밀번호를 입력해 주세요");
        $password.focus();
        isValid = false;
    }

    return isValid;
}

// 그룹 생성 폼 유효성 검사 (교사권한 사용자)
export function validateCreateGroupForm($form) {
    let isValid = true;
    const $title = $form.find('input[name="title"]');

    // 폼 인풋 타이틀 및 에러 메세지 초기화
    allClearFormErrorMessage($form);

    // 유효성 검사
    if ($title.val().trim() === "") {
        addInputErrorMessage($title, "우리반 이름을 입력해 주세요");
        $title.focus();
        isValid = false;
    }

    return isValid;
}

// 그룹 수정 폼 유효성 검사 (교사권한 사용자)
export function validateModifyGroupForm($form) {
    let isValid = true;
    const $title = $form.find('input[name="title"]');

    // 폼 인풋 타이틀 및 에러 메세지 초기화
    allClearFormErrorMessage($form);

    // 유효성 검사
    if ($title.val().trim() === "") {
        addInputErrorMessage($title, "우리반 이름을 입력해 주세요");
        $title.focus();
        isValid = false;
    }

    return isValid;
}

// 그룹 삭제 폼 유효성 검사 (교사권한 사용자)
export function validateDeleteGroupForm($form) {
    let isValid = true;

    // 그룹 삭제 동의 여부
    const isDeleteCheck = $form.find("input[name='deleteGroup']").is(":checked");

    // 유효성 검사
    if (!isDeleteCheck) {
        alert("우리반 삭제는 복구가 불가능합니다.\n삭제를 원하실 경우 '예'를 선택해 주세요.");
        isValid = false;
    }

    return isValid;
}