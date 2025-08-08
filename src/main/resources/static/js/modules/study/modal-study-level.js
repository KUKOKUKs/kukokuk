import {elementarySchool, middleSchool} from '../../utils/handler-util.js';
import {getStudyDifficultyList} from "./study-api.js";

$(document).ready(async function () {
    // 로컬스토리지에서 studyDifficultyList 값 가져오기
    let studyDifficultyList = localStorage.getItem("studyDifficultyList");
    console.log("localStorage studyDifficultyList: ", studyDifficultyList);

    if (!studyDifficultyList) {
        // 값이 없을 경우
        try {
            studyDifficultyList = await getStudyDifficultyList(); // 비동기로 데이터 요청

            if (!studyDifficultyList) {
                console.log("학습 단계 정보 비동기 요청 반환 값이 없습니다.");
                localStorage.removeItem("studyDifficultyList"); // 로컬스토리지에서 삭제
            } else {
                const jsonStudyDifficultyList = JSON.stringify(studyDifficultyList);
                localStorage.setItem("studyDifficultyList", jsonStudyDifficultyList); // 데이터를 로컬스토리지에 저장
                setStudyDifficultyList(jsonStudyDifficultyList); // study-difficulty에 리스트 추가
                console.log("studyDifficultyList setting 완료");
            }
        } catch (error) {
            // 요청 에러
            console.error(error);
        }
    } else {
        setStudyDifficultyList(JSON.parse(studyDifficultyList)); // study-difficulty에 리스트 추가
    }

    // study-difficulty에 리스트 추가
    function setStudyDifficultyList(studyDifficultyList) {
        console.log("study-difficulty에 리스트 추가 setStudyDifficultyList() 실행 studyDifficultyList: ", studyDifficultyList);

        const $studyDifficultyElement = $("#study-difficulty"); // 리스트가 추가될 부모 요소
        let content = "";

        for (let studyDifficulty of studyDifficultyList) {
            content += `
                <div class="modal_explanation">
                    <p class="explanation_title">${studyDifficulty.studyDifficultyNo}단계</p>
                    <p class="explanation">${studyDifficulty.explanation}</p>
                </div>
            `;
        }

        // 부모 요소에 리스트 추가
        $studyDifficultyElement.html(content);
    }

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

    // 사용자 진도/단계 선택 모달창 열기
    const $modalStudyLevelBtn = $("#modal-study-level-btn"); // 모달창 열기 버튼
    const $modalStudyLevel = $("#modal-study-level"); // 모달창
    $modalStudyLevelBtn.click(function () {
        if ($modalStudyLevel.length) {
            // 해당 모달창 요소가 있을 경우 열기
            $modalStudyLevel.show();
        }
    });
});