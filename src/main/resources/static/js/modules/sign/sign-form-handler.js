import {validateLoginForm} from '/js/modules/sign/sign-form-validator.js';
import {
    addInputErrorMessage,
    clearInputErrorMessage
} from '/js/util/form-error-util.js';

$(document).ready(() => {
    const $loginForm = $("#login-form"); // 로그인 폼
    const $email = $loginForm.find("input[name='username'"); // username input
    const $password = $loginForm.find("input[name='password'"); // password input
    const $inputDeleteBtn = $(".input_delete_btn"); // 인풋 값 초기화 버튼
    const $inputPasswordViewBtn = $("#input-password-view-btn"); // password 타입 토글 버튼

    // 폼 제출 이벤트 발생 시 유효성 검사 후 제출
    $loginForm.submit(function (e) {
        e.preventDefault();
        const $form = $(this);

        // 유효성 검사 통화 시 제출
        if (validateLoginForm($form)) {
            this.submit();
        }
    });

    // 실시간 인풋 유효성 검사(로그인폼에서는 입력 유무만 체크)
    $email.on("input", function () {
        if (this.value === "") {
            addInputErrorMessage($email, "이메일을 입력해 주세요");
        } else {
            clearInputErrorMessage($email);
        }
    });

    $password.on("input", function () {
        if (this.value === "") {
            addInputErrorMessage($password, "비밀번호를 입력해 주세요");
        } else {
            clearInputErrorMessage($password);
        }
    });

    // sign form 내부 아이콘 버튼으로 해당하는 input 값 초기화
    $inputDeleteBtn.click(function () {
        // 클릭한 요소의 가장 가까운 .input_info 요소의 자식요소 input 찾기
        const $input = $(this).closest(".input_info").find("input");
        $input.val(""); // 값 초기화
        $input.focus(); // 다시 포커스 유지
    });

    // sign form 내부 패스워드 보기 버튼으로 해당하는 input type 토글
    $inputPasswordViewBtn.click(function () {
        // 클릭한 요소의 가장 가까운 .input_info 요소의 자식요소 input 찾기
        const $passwordInput = $(this).closest(".input_info").find("input");
        const inputType = $passwordInput.attr("type");  // 인풋 타입 가져오기
        const isPassword = inputType === "password";

        // 인풋 타입 토글
        $passwordInput.attr("type", isPassword ? "text" : "password");

        // 클릭한 요소의 아이콘 토글
        const $icon = $(this).find("iconify-icon");
        $icon.attr("icon",
            isPassword ? "clarity:eye-show-solid" : "clarity:eye-hide-solid");
    });
});
