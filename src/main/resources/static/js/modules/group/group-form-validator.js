// 프로필 수정 폼 제출 시 유효성 검사
import {
    addInputErrorMessage,
    allClearFormErrorMessage
} from "../../utils/form-error-util.js";

// 그룹 생성 폼 유효성 검사
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

// 그룹 수정 폼 유효성 검사
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