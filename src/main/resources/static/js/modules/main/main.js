// noinspection ES6UnusedImports

import {elementarySchool, middleSchool} from '/js/utils/handler-util.js';

$(document).ready(() => {
    // 사용자 진도/단계 설정 관련
    const $modalStudyLevelForm = $("#modal-study-level-form"); // 진도/단계 설정 폼
    const $modalStudyLevelSchool = $modalStudyLevelForm.find("select[name='currentSchool']"); // currentSchool select
    const $modalStudyLevelGrade = $modalStudyLevelForm.find("select[name='currentGrade']"); // currentGrade select
    const $modalStudyLevelDifficulty = $modalStudyLevelForm.find("select[name='studyDifficulty']"); // studyDifficulty select
    const $modalStudyLevelSubmitBtn = $modalStudyLevelForm.find("button[type='submit']"); // $modalStudyLevelForm submit button

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
});