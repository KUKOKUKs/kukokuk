import {
    elementarySchool,
    middleSchool,
    setStudyDifficultyList
} from '../../utils/handler-util.js';

$(document).ready(async function () {
    // 사용자 진도/단계 설정 관련
    const $modalStudyLevelForm = $("#modal-study-level-form"); // 진도/단계 설정 폼
    const $modalStudyLevelSchool = $modalStudyLevelForm.find("select[name='currentSchool']"); // currentSchool select
    const $modalStudyLevelGrade = $modalStudyLevelForm.find("select[name='currentGrade']"); // currentGrade select
    const $modalStudyLevelDifficulty = $modalStudyLevelForm.find("select[name='studyDifficulty']"); // studyDifficulty select
    const $modalStudyLevelSubmitBtn = $modalStudyLevelForm.find("button[type='submit']"); // $modalStudyLevelForm submit button
    const $modalStudyDifficultyInfoElement = $modalStudyLevelForm.find("#study-difficulty"); // 단계별 설명 리스트가 추가될 부모 요소

    // 진도 선택 핸들러
    $modalStudyLevelSchool.on("change", function () {
        const val = $(this).val();

        // grade 옵션 세팅
        if (val === "초등") {
            $modalStudyLevelGrade.html(elementarySchool);
        } else if (val === "중등") {
            $modalStudyLevelGrade.html(middleSchool);
        }

        // 제출 버튼 활성화
        if (val && $modalStudyLevelGrade.val() && $modalStudyLevelDifficulty.val()) {
            $modalStudyLevelSubmitBtn.removeClass("disabled");
        }
    });

    // 학년 선택 핸들러
    $modalStudyLevelGrade.on("change", function () {
        const val = $(this).val();

        // 제출 버튼 활성화
        if (val && $modalStudyLevelSchool.val() && $modalStudyLevelDifficulty.val()) {
            $modalStudyLevelSubmitBtn.removeClass("disabled");
        }
    });

    // 단계 선택 핸들러
    $modalStudyLevelDifficulty.on("change", function () {
        const val = $(this).val();

        // 제출 버튼 활성화
        if (val && $modalStudyLevelSchool.val() && $modalStudyLevelGrade.val()) {
            $modalStudyLevelSubmitBtn.removeClass("disabled");
        }
    });

    // 사용자 진도/단계 선택 모달창 열기
    const $modalStudyLevelBtn = $(".modal_study_level_btn"); // 모달창 열기 버튼
    const $modalStudyLevel = $("#modal-study-level"); // 모달창
    $modalStudyLevelBtn.click(function () {
        if ($modalStudyLevel.length) {
            // 해당 모달창 요소가 있을 경우 열기
            $modalStudyLevel.show();

            // 모달창 단계별 설명 리스트 추가
            setStudyDifficultyList($modalStudyDifficultyInfoElement);

            // 약간의 딜레이를 주어 show 후 css transition 적용될 수 있도록 함
            setTimeout(() => {
                $modalStudyLevel.addClass("open");
            }, 10);
        }
    });
});