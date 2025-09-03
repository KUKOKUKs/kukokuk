// noinspection ES6UnusedImports

import {validateProfileForm} from './profile-form-validator.js';
import {apiCheckNicknameDuplicate,} from '../sign/sign-api.js';
import {
    addInputErrorMessage,
    clearInputErrorMessage
} from '../../utils/form-error-util.js';
import {debounce} from '../../utils/debounce-util.js';
import {
    elementarySchool,
    middleSchool,
    setStudyDifficultyList
} from '../../utils/handler-util.js';
import {regExNickname, validateDate} from '../../utils/validation-util.js';

$(document).ready(function () {
    // 프로필 설정 관련
    const $userProfileImgUpdateForm = $("#profile-img-update-form"); // 프로필 이미지 업데이트 폼
    const $userProfileImgDeleteForm = $("#profile-img-delete-form"); // 프로필 이미지 삭제 폼
    const $userUpdateProfileImg = $userProfileImgUpdateForm.find("input[name='profileFile']"); // profileFile input
    const $userUpdateForm = $("#profile-update-form"); // 프로필 설정 폼
    const $userUpdateName = $userUpdateForm.find("input[name='name']"); // name input
    const $userUpdateNickname = $userUpdateForm.find("input[name='nickname']"); // nickname input
    const $userUpdateBirthDate = $userUpdateForm.find("input[name='birthDate']"); // birthDate input
    const $userUpdateCurrentNickname = $userUpdateForm.find("input[name='currentNickname']"); // currentNickname input
    const $userUpdateCurrentSchool = $userUpdateForm.find("select[name='currentSchool']"); // currentSchool select
    const $userUpdateCurrentGrade = $userUpdateForm.find("select[name='currentGrade']"); // currentGrade select
    const $modalUserUpdateStudyDifficultyInfoElement = $userUpdateForm.find("#study-difficulty"); // 단계별 설명 리스트가 추가될 부모 요소

    // 프로필 설정 폼 제출 이벤트 발생 시 유효성 검사 후 제출
    $userUpdateForm.submit(function (e) {
        e.preventDefault();

        // 유효성 검사 통화 시 제출
        if (validateProfileForm($(this))) {
            this.submit();
        }
    });
    
    // 프로필 이미지 선택 핸들러
    $userUpdateProfileImg.on("change", function (e) {
        const file = this.files[0];
        if (!file) return false;

        const validateTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/heic']; // 선택 가능 확장자
        const maxFileSize = 1024 * 1024; // 5MB 제한

        // 파일 형식 유효성 검사
        if (!validateTypes.includes(file.type)) {
            alert("지원하지 않는 이미지 형식입니다.");
            this.value = ""; // 선택 초기화
            return false;
        }

        // 파일 크기 유효성 검사
        if (file.size > maxFileSize) {
            alert("이미지 크기는 5MB를 초과할 수 없습니다.");
            this.value = ""; // 선택 초기화
            return false;
        }

        const isConfirm = confirm("선택한 이미지로 등록하시겠습니까?");
        if (!isConfirm) {
            this.value = ""; // 선택 초기화
            return false;
        }

        // 유효성 검사 통과 시 폼 제출
        $userProfileImgUpdateForm.submit();
    });

    // 프로필 이미지 삭제 폼 제출 이벤트 발생 시 Confirm 후 제출
    $userProfileImgDeleteForm.submit(function () {
        const isConfirm = confirm('이 작업은 되돌릴 수 없습니다.\n프로필 이미지를 정말 삭제하시겠습니까?');

        // 취소 시 리턴
        if (!isConfirm) return false;

        this.submit();
    });

    // nickname 중복 체크
    async function handleNicknameInput(nickname) {
        clearInputErrorMessage($userUpdateNickname); // 에러 메세지 초기화
        const isDuplicated = await apiCheckNicknameDuplicate(nickname);
        console.log("handleNicknameInput 실행 결과: ", isDuplicated);

        const isValid = !isDuplicated; // true=중복, false=중복이 아니므로 !로 적용

        if (!isValid) {
            addInputErrorMessage($userUpdateNickname, "사용중인 닉네임입니다");
        }
    }

    // 중복 체크 요청
    const nicknameCheckDebounce = debounce(handleNicknameInput, 500); // nickname

    // 프로필 설정 이름 유효성 검증 처리
    $userUpdateName.on("input blur", function () {
        clearInputErrorMessage($userUpdateName); // 에러 메세지 초기화
        const val = $(this).val();

        if (val === "") {
            addInputErrorMessage($userUpdateName, "이름을 입력해 주세요");
        }
    });

    // 프로필 설정 닉네임 유효성 검증 처리
    $userUpdateNickname.on("input blur", function () {
        clearInputErrorMessage($userUpdateNickname); // 에러 메세지 초기화
        const val = $(this).val();

        if (val === "") {
            addInputErrorMessage($userUpdateNickname, "닉네임을 입력해 주세요");
        } else if (!regExNickname.test(val)) {
            // 정규표현식을 통과하지 못한 경우
            addInputErrorMessage($userUpdateNickname, "유효한 닉네임 형식이 아닙니다");
        } else if (val !== $userUpdateCurrentNickname.val()) {
            // 사용자 현재 닉네임과 입력한 닉네임이 같지 않을 경우 중복 검사
            nicknameCheckDebounce(val);
        }
    });

    // 프로필 설정 생년월일 유효성 검증 처리
    $userUpdateBirthDate.on("input blur", function () {
        clearInputErrorMessage($userUpdateBirthDate); // 에러 메세지 초기화

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
            addInputErrorMessage($userUpdateBirthDate, "생년월일을 확인해 주세요");
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
                    addInputErrorMessage($userUpdateBirthDate, "유효한 생년월일이 아닙니다");
                } else {
                    const formattedDate = `${y}-${m}-${d}`;
                    $(this).val(formattedDate);
                }
            } else {
                addInputErrorMessage($userUpdateBirthDate, "유효한 생년월일이 아닙니다");
            }
        }
    });

    // 프로필 설정 진도 선택 핸들러
    $userUpdateCurrentSchool.on("change", function () {
        const val = $(this).val();

        // grade 옵션 세팅
        if (val === "초등") {
            $userUpdateCurrentGrade.html(elementarySchool);
        } else if (val === "중등") {
            $userUpdateCurrentGrade.html(middleSchool);
        }
    });

    // 단계별 설명 리스트 모달창 열기
    const $modalStudyDifficultyInfoBtn = $("#modal-study-difficulty-info-btn"); // 모달창 열기 버튼
    const $modalStudyDifficultyInfo = $("#modal-study-difficulty-info"); // 모달창
    $modalStudyDifficultyInfoBtn.click(async function () {
        if ($modalStudyDifficultyInfo.length) {
            // 해당 모달창 요소가 있을 경우 열기
            $modalStudyDifficultyInfo.show();

            // 모달창 단계별 설명 리스트 추가
            await setStudyDifficultyList($modalUserUpdateStudyDifficultyInfoElement);

            // 약간의 딜레이를 주어 show 후 css transition 적용될 수 있도록 함
            setTimeout(() => {
                $modalStudyDifficultyInfo.addClass("open");
            }, 10);
        }
    });
});