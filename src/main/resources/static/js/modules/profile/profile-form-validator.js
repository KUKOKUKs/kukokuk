import {
    addInputErrorMessage,
    allClearFormErrorMessage
} from "/js/utils/form-error-util.js";

import {regExNickname, validateBirthDate} from '/js/utils/validation-util.js';

// 프로필 수정 폼 제출 시 유효성 검사
export function validateProfileForm($form) {
    let isValid = true;
    const $name = $form.find('input[name="name"]');
    const $nickname = $form.find('input[name="nickname"]');
    const $birthDate = $form.find('input[name="birthDate"]');

    // 폼 인풋 타이틀 및 에러 메세지 초기화
    allClearFormErrorMessage($form);

    // 유효성 검사
    if ($name.val().trim() === "") {
        addInputErrorMessage($name, "이름을 입력해 주세요");
        $name.focus();
        isValid = false;
    }

    if ($nickname.val().trim() === "") {
        addInputErrorMessage($nickname, "닉네임을 입력해 주세요");
        $nickname.focus();
        isValid = false;

    } else if (!regExNickname.test($nickname.val().trim())) {
        addInputErrorMessage($nickname, "닉네임을 확인해 주세요");
        $nickname.focus();
        isValid = false;
    }

    if ($birthDate.val().trim() === "") {
        addInputErrorMessage($birthDate, "생년월일을 입력해 주세요");
        $birthDate.focus();
        isValid = false;

    } else if (!validateBirthDate($birthDate.val().trim())) {
        addInputErrorMessage($birthDate, "생년월일을 확인해 주세요");
        $birthDate.focus();
        isValid = false;
    }

    return isValid;
}
