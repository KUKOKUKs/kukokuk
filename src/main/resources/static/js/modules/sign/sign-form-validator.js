import {
    addInputErrorMessage,
    allClearFormErrorMessage
} from "/js/utils/form-error-util.js";

// 로그인 폼 제출 시 유효성 검사
export function validateLoginForm($form) {
    let isValid = true;
    const $email = $form.find('input[name="username"]');
    const $password = $form.find('input[name="password"]');

    // 폼 인풋 타이틀 및 에러 메세지 초기화
    allClearFormErrorMessage($form);

    // 유효성 검사
    if ($password.val().trim() === "") {
        addInputErrorMessage($password, "비밀번호를 입력해 주세요");
        $password.focus();
        isValid = false;
    }

    if ($email.val().trim() === "") {
        addInputErrorMessage($email, "이메일을 입력해 주세요");
        $email.focus();
        isValid = false;
    }

    return isValid;
}
