import {apiGetStudyDifficultyList} from "../modules/study/study-api.js";

const questLinkByContentType = {
    "STUDY": "/study",
    "ESSAY": "/study",
    "SPEED": "/quiz",
    "LEVEL": "/quiz",
    "DICTATION": "/quiz"
}

// 일일 도전과제 해당 타입의 경로 설정
export function replaceQuestLinkByContentType(contentType) {
    return questLinkByContentType[contentType];
}

// 1~3, 1~6  학년 옵션 생성
export const elementarySchool = setGradeOptions(6);
export const middleSchool = setGradeOptions(3);

/**
 * 진도별 학년 옵션 개수를 전달받아 생성한 옵션을 반환 
 * @param count 생성할 학년 옵션 수
 * @returns 생성된 옵션 리스트(string)
 */
export function setGradeOptions(count) {
    let options = '';
    for (let i = 1; i <= count; i++) {
        options += `<option value="${i}">${i}학년</option>`;
    }
    return options;
}

/**
 * 전달받은 요소에 단계별 설명 리스트를 비동기 요청하여 추가하고 로컬스토리지에 저장
 * 로컬스토리지에 저장하여 불필요한 중복 요청 방지
 * @param $studyDifficultyElement 리스트르 추가할 요소(jQuery 선택자)
 */
export async function setStudyDifficultyList($studyDifficultyElement) {
    console.log("setStudyDifficultyList() 실행");
    
    // .modal_explanation 클라스 자식요소로 있을 경우 이미 설정 완료로 판단
    const isSetInfo = $studyDifficultyElement.find(".modal_explanation").length > 0;
    if (isSetInfo) return false;

    // 로컬스토리지에서 studyDifficultyList 값 가져오기
    let studyDifficultyList = localStorage.getItem("studyDifficultyList");
    console.log("localStorage studyDifficultyList: ", studyDifficultyList);

    if (!studyDifficultyList) {
        // 값이 없을 경우
        try {
            studyDifficultyList = await apiGetStudyDifficultyList(); // 비동기로 단계별 설명 리스트 요청

            if (!studyDifficultyList) {
                console.log("학습 단계 정보 비동기 요청 반환 값이 없습니다.");
                localStorage.removeItem("studyDifficultyList"); // 로컬스토리지에서 삭제
            } else {
                localStorage.setItem("studyDifficultyList", JSON.stringify(studyDifficultyList)); // JSON 데이터를 문자열화하여 로컬스토리지에 저장
                addStudyDifficultyList($studyDifficultyElement, studyDifficultyList); // $studyDifficultyElement에 리스트 추가
                console.log("studyDifficultyList setting 완료");
            }
        } catch (error) {
            // 요청 에러
            console.error(error);
        }
    } else {
        // 문자열화된 로컬스토리지의 값을 JSON화하여 적용
        addStudyDifficultyList($studyDifficultyElement, JSON.parse(studyDifficultyList)); // study-difficulty에 리스트 추가
    }
}

/**
 * 전달 받은 요소에 함수내 content 양식으로 학습 단계별 설명 리스트 추가
 * @param $studyDifficultyElement 리스트르 추가할 요소(jQuery 선택자)
 * @param studyDifficultyList 단계별 설명 리스트(List<StudyDifficulty>객체/JSON 형식)
 */
export function addStudyDifficultyList($studyDifficultyElement, studyDifficultyList) {
    console.log("study-difficulty에 리스트 추가 setStudyDifficultyList() 실행 studyDifficultyList: ", studyDifficultyList);

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