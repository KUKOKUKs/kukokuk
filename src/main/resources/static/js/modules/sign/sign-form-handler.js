import {validateLoginForm} from '/js/modules/sign/sign-form-validator.js';
import {
    addInputErrorMessage,
    clearInputErrorMessage
} from '/js/util/form-error-util.js';

$(document).ready(() => {
    // sign 공통 관련
    let isFormValid = false; // 폼 유효성 검증 플래그
    const regExEmail = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z.]{2,5}$/; // 이메일 정규표현식
    const $inputDeleteBtn = $(".input_delete_btn"); // 인풋 값 초기화 버튼
    const $inputPasswordViewBtn = $("#input-password-view-btn"); // password 타입 토글 버튼

    // 로그인 관련
    const $loginForm = $("#login-form"); // 로그인 폼
    const $loginEmail = $loginForm.find("input[name='username']"); // username input
    const $loginPassword = $loginForm.find("input[name='password']"); // password input

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
    $loginEmail.on("input blur", function () {
        if (this.value === "") {
            addInputErrorMessage($loginEmail, "이메일을 입력해 주세요");
        } else {
            clearInputErrorMessage($loginEmail);
        }
    });

    $loginPassword.on("input blur", function () {
        if (this.value === "") {
            addInputErrorMessage($loginPassword, "비밀번호를 입력해 주세요");
        } else {
            clearInputErrorMessage($loginPassword);
        }
    });
    
    // 회원가입 관련
    // const $registerForm = $("#register-form"); // 회원가입 폼
    // const $registerSubmitBtn = $("button[type='submit']");
    // const $registerEmail = $registerForm.find("input[name='username']"); // username input
    // const $registerPassword = $registerForm.find("input[name='password']"); // password input
    //
    // // 회원가입 이메일 유효성 검증 처리
    // $registerEmail.on("input blur", function () {
    //     clearInputErrorMessage($registerEmail); // 에러 메세지 초기화
    //     $registerSubmitBtn.removeClass("disabled"); // 제출 버튼 활성 초기화
    //     isFormValid = true; // 폼 유효성 검증 플래그
    //
    //     if (this.value === "") {
    //         // 값이 없을 경우
    //         addInputErrorMessage($registerEmail, "이메일을 입력해 주세요");
    //         isFormValid = false;
    //     } else if (!regExEmail.test($registerEmail.val())) {
    //         // 정규표현식을 통과하지 못한 경우
    //         addInputErrorMessage($registerEmail, "유효한 이메일 형식이 아닙니다");
    //         isFormValid = false;
    //     }
    //
    //     // 유효하지 않을 경우 제출 버튼 비활성화
    //     if (!isFormValid) $registerSubmitBtn.addClass("disabled");
    // });

    // sign form 내부 아이콘 버튼으로 해당하는 input 값 초기화
    $inputDeleteBtn.click(function () {
        // 클릭한 요소의 가장 가까운 .input_info 요소의 자식요소 input 찾기
        const $input = $(this).closest(".input_info").find("input");
        clearInputErrorMessage($input); // 에러 메세지 초기화
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
