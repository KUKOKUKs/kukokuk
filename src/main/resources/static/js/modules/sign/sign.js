// noinspection ES6UnusedImports

import {validateLoginForm} from './sign-form-validator.js';
import {checkNicknameDuplicate, checkUsernameDuplicate} from './sign-api.js';
import {
    addInputErrorMessage,
    clearInputErrorMessage
} from '../../utils/form-error-util.js';
import {
    regExEmail,
    regExNickname,
    regExPassword,
    validateBirthDate,
    validateDate
} from '../../utils/validation-util.js';
import {debounce} from '../../utils/debounce-util.js';

$(document).ready(() => {
    let isValid = false; // 폼 내부 인풋 유효성 검증 플래그
    const $submitBtn = $("button[type='submit']");
    const $inputDeleteBtn = $(".input_delete_btn"); // 인풋 값 초기화 버튼
    const $inputPasswordViewBtn = $(".input-password-view-btn"); // password 타입 토글 버튼

    // 회원가입 관련
    const $registerForm = $("#register-form"); // 회원가입 폼
    const $registerEmail = $registerForm.find("input[name='username']"); // username input
    const $registerPassword = $registerForm.find("input[name='password']"); // password input
    const $registerPasswordConfirm = $registerForm.find("input[name='passwordConfirm']"); // passwordConfirm input
    const $registerName = $registerForm.find("input[name='name']"); // name input
    const $registerBirthDate = $registerForm.find("input[name='birthDate']"); // birthDate input
    const $registerNickname = $registerForm.find("input[name='nickname']"); // nickname input

    // 회원가입 폼 제출 이벤트 발생 시 유효성 검사 후 제출
    $registerForm.submit(function (e) {
        e.preventDefault();
        if (!isValid) return false;

        // 유효성 검사 통화 시 제출
        this.submit();
    });

    // username(email) 중복 체크
    async function handleEmailInput(username) {
        clearInputErrorMessage($registerEmail); // 에러 메세지 초기화

        try {
            const isDuplicated = await checkUsernameDuplicate(username);
            console.log("handleEmailInput 실행 결과: ", isDuplicated);
            isValid = !isDuplicated; // true=중복, false=중복이 아니므로 !로 적용

            if (!isValid) {
                addInputErrorMessage($registerEmail, "사용중인 이메일입니다");
            }
        } catch (error) {
            console.error(error);
            addInputErrorMessage($registerEmail, "중복 확인 요청에 실패했습니다");
            isValid = false;
            return false;
        }

        // 제출 버튼 활성화/비활성화 설정
        handleSubmitBtn(isValid);
    }

    // nickname 중복 체크
    async function handleNicknameInput(nickname) {
        clearInputErrorMessage($registerNickname); // 에러 메세지 초기화

        try {
            const isDuplicated = await checkNicknameDuplicate(nickname);
            console.log("handleNicknameInput 실행 결과: ", isDuplicated);
            isValid = !isDuplicated; // true=중복, false=중복이 아니므로 !로 적용

            if (!isValid) {
                addInputErrorMessage($registerNickname, "사용중인 닉네임입니다");
            }
        } catch (error) {
            console.error(error);
            addInputErrorMessage($registerEmail, "중복 확인 요청에 실패했습니다");
            isValid = false;
            return false;
        }

        // 제출 버튼 활성화/비활성화 설정
        handleSubmitBtn(isValid);
    }

    // 중복 체크 요청
    const emailCheckDebounce = debounce(handleEmailInput, 500); // email
    const nicknameCheckDebounce = debounce(handleNicknameInput, 500); // nickname

    // 회원가입 이메일 유효성 검증 처리
    $registerEmail.on("input blur", function () {
        clearInputErrorMessage($registerEmail); // 에러 메세지 초기화
        isValid = false; // 폼 내부 인풋 유효성 검증 플래그
        const val = $(this).val();

        if (val === "") {
            // 값이 없을 경우
            addInputErrorMessage($registerEmail, "이메일을 입력해 주세요");
        } else if (!regExEmail.test(val)) {
            // 정규표현식을 통과하지 못한 경우
            addInputErrorMessage($registerEmail, "유효한 이메일 형식이 아닙니다");
        } else {
            isValid = true;
        }

        if (isValid) {
            // 기본 유효성 검증 통과 시 중복 검사 실행
            emailCheckDebounce(val);
        } else {
            // 제출 버튼 활성화/비활성화 설정
            handleSubmitBtn(isValid);
        }
    });

    // 회원가입 비밀번호 유효성 검증 처리
    $registerPassword.on("input blur", function () {
        clearInputErrorMessage($registerPassword); // 에러 메세지 초기화
        isValid = false; // 폼 내부 인풋 유효성 검증 플래그
        const val = $(this).val();

        if (val === "") {
            // 값이 없을 경우
            addInputErrorMessage($registerPassword, "비밀번호를 입력해 주세요");
        } else if (!regExPassword.test(val)) {
            // 정규표현식을 통과하지 못한 경우
            addInputErrorMessage($registerPassword, "유효한 비밀번호 형식이 아닙니다");
        } else if ($registerPasswordConfirm.val().trim() !== "" && val !== $registerPasswordConfirm.val()) {
            // 비빌번호 확인란이 비어있지 않고 비밀번호가 비빌번호 확인 값이 같지 않을 경우
            addInputErrorMessage($registerPasswordConfirm, "비밀번호가 일치하지 않습니다");
        } else {
            isValid = true;
        }

        // 제출 버튼 활성화/비활성화 설정
        handleSubmitBtn(isValid);
    });

    // 회원가입 비밀번호 확인 유효성 검증 처리
    $registerPasswordConfirm.on("input blur", function () {
        clearInputErrorMessage($registerPasswordConfirm); // 에러 메세지 초기화
        isValid = false; // 폼 내부 인풋 유효성 검증 플래그
        const val = $(this).val();

        if (val === "") {
            // 값이 없을 경우
            addInputErrorMessage($registerPasswordConfirm, "비밀번호를 다시 입력해 주세요");
        } else if ($registerPassword.val().trim() !== "" && $registerPassword.val() !== val) {
            // 비빌번호 확인란이 비어있지 않고 비밀번호가 비빌번호 확인 값이 같지 않을 경우
            addInputErrorMessage($registerPasswordConfirm, "비밀번호가 일치하지 않습니다");
        }  else {
            isValid = true;
        }

        // 제출 버튼 활성화/비활성화 설정
        handleSubmitBtn(isValid);
    });

    // 회원가입 이름 유효성 검증 처리
    $registerName.on("input blur", function () {
        clearInputErrorMessage($registerName); // 에러 메세지 초기화
        isValid = false; // 폼 내부 인풋 유효성 검증 플래그
        const val = $(this).val();
        
        if (val === "") {
            addInputErrorMessage($registerName, "이름을 입력해 주세요");
        }  else {
            isValid = validateBirthDate($registerBirthDate.val());
        }

        // 제출 버튼 활성화/비활성화 설정
        handleSubmitBtn(isValid);
    });

    // 회원가입 생년월일 유효성 검증 처리
    $registerBirthDate.on("input blur", function () {
        clearInputErrorMessage($registerBirthDate); // 에러 메세지 초기화
        isValid = false; // 폼 내부 인풋 유효성 검증 플래그

        // 숫자가 아닌 값이 입력될 경우 실시간 제거
        const original = $(this).val();
        const digitsOnly = original.replace(/\D/g, ''); // 숫자만 추출
        const current = digitsOnly.slice(0, 8); // 최대 8자리까지 유지

        // 현재 값이 8자리가 아니면 하이픈 제거
        if (current.length < 8 && original !== current) {
            // 같지 않다면 숫자 이외의 값이 입력이 되었다고 판단
            $(this).val(current); // 숫자 이외 제거
        }

        // 값이 비었거나 8자리 이하일 경우
        if (current === "" || current.length < 8) {
            addInputErrorMessage($registerBirthDate, "생년월일을 확인해 주세요");
        } else {
            const y = current.slice(0, 4);
            const m = current.slice(4, 6);
            const d = current.slice(6, 8);

            // 8자리 숫자가 입력되었고 유효한 날짜인 경우 yyyy-MM-dd 형식으로 변경
            if (validateDate(y, m, d)) {
                const inputDate = new Date(`${y}-${m}-${d}`);
                const today = new Date();
                today.setHours(0, 0, 0, 0); // 시간을 0으로 설정하여 비교 안정성 확보

                // 오늘 이전 날짜인지 확인
                if (inputDate >= today) {
                    addInputErrorMessage($registerBirthDate, "유효한 생년월일이 아닙니다");
                } else {
                    const formattedDate = `${y}-${m}-${d}`;
                    $registerBirthDate.val(formattedDate);
                    isValid = $registerName.val() !== "";
                }
            } else {
                addInputErrorMessage($registerBirthDate, "유효한 생년월일이 아닙니다");
            }
        }

        // 제출 버튼 활성화/비활성화 설정
        handleSubmitBtn(isValid);
    });

    // 회원가입 닉네임 유효성 검증 처리
    $registerNickname.on("input blur", function () {
        clearInputErrorMessage($registerNickname); // 에러 메세지 초기화
        isValid = false; // 폼 내부 인풋 유효성 검증 플래그
        const val = $(this).val();

        if (val === "") {
            addInputErrorMessage($registerNickname, "닉네임을 입력해 주세요");
        } else if (!regExNickname.test(val)) {
            // 정규표현식을 통과하지 못한 경우
            addInputErrorMessage($registerNickname, "유효한 닉네임 형식이 아닙니다");
        } else {
            isValid = true;
        }

        if (isValid) {
            // 기본 유효성 검증 통과 시 중복 검사 실행
            nicknameCheckDebounce(val);
        } else {
            // 제출 버튼 활성화/비활성화 설정
            handleSubmitBtn(isValid);
        }
    });

    // 로그인 관련
    const $loginForm = $("#login-form"); // 로그인 폼
    const $loginEmail = $loginForm.find("input[name='username']"); // username input
    const $loginPassword = $loginForm.find("input[name='password']"); // password input

    // 로그인 폼 제출 이벤트 발생 시 유효성 검사 후 제출
    $loginForm.submit(function (e) {
        e.preventDefault();

        // 유효성 검사 통화 시 제출
        if (validateLoginForm($(this))) {
            this.submit();
        }
    });

    // 실시간 인풋 유효성 검사(로그인폼에서는 입력 유무만 체크)
    $loginEmail.on("input blur", function () {
        clearInputErrorMessage($loginEmail);
        isValid = false; // 인풋 유효성 검증 플래그
        const val = $(this).val();

        if (val === "") {
            // 값이 없을 경우
            addInputErrorMessage($loginEmail, "이메일을 입력해 주세요");
        } else if (val !== "" && $loginPassword.val().trim() !== "") {
            isValid = true;
        }

        // 제출 버튼 활성화/비활성화 설정
        handleSubmitBtn(isValid);
    });

    $loginPassword.on("input blur", function () {
        clearInputErrorMessage($loginPassword);
        isValid = false; // 인풋 유효성 검증 플래그
        const val = $(this).val();

        if (val === "") {
            // 값이 없을 경우
            addInputErrorMessage($loginPassword, "비밀번호를 입력해 주세요");
        } else if (val !== "" && $loginEmail.val().trim() !== "") {
            isValid = true;
        }

        // 제출 버튼 활성화/비활성화 설정
        handleSubmitBtn(isValid);
    });

    // 제출 버튼 활성화 핸들러
    function handleSubmitBtn(isValid) {
        if (isValid) {
            $submitBtn.removeClass("disabled");
        } else {
            $submitBtn.addClass("disabled");
        }
    }

    // sign form 내부 아이콘 버튼으로 해당하는 input 값 초기화
    $inputDeleteBtn.click(function () {
        // 클릭한 요소의 가장 가까운 .input_info 요소의 자식요소 input 찾기
        const $input = $(this).closest(".input_info").find("input");
        clearInputErrorMessage($input); // 에러 메세지 초기화
        $input.val(""); // 값 초기화
        $input.focus(); // 다시 포커스 유지
        $submitBtn.addClass("disabled"); // 제출 버튼 비활성화
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
        $icon.attr("icon", isPassword ? "clarity:eye-show-solid" : "clarity:eye-hide-solid");
    });
});
