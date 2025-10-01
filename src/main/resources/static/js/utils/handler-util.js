import {apiGetStudyDifficultyList} from "../modules/study/study-api.js";

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

/**
 * 페이지네이션 정보와 페이징요소가 추가될 부모 요소를 전달 받아
 * 이미 변경 작업 된 url 정보를 가져와 href를 적용한 페이징 요소를 부모 요소에 세팅
 * 비동기로 처리하는 페이징일 경우 href의 쿼리스트링을 활용하여 해당 버튼에 이벤트로 실행
 * @param paging 페이지네이션 정보
 * @param $parentElement 페이징 요소가 추가될 부모 요소(컨텐츠 특정 필수)
 */
export function setPagination(paging, $parentElement) {
    console.log("setPagination() paging: ", paging);

    const path = location.pathname; // 현재 경로

    // 페이지네이션이 null이 아니고 총 페이지 개수가 1보다 클 경우
    if (paging && paging.totalPages > 1) {
        // 기존 요소 확인
        let $paginationElement = $parentElement.find(".pagination");

        // 없으면 생성 후 부모에 추가
        if (!$paginationElement.length) {
            $paginationElement = $("<nav>").addClass("pagination");
            $parentElement.append($paginationElement);
        }

        // 페이징 요소
        const $paging = $("<ul>").addClass("paging");

        // 항상 새로운 params 객체 생성(중복/누적 처리된 키가 적용되지 않도록)
        const makeParams = (pageNum) => {
            const params = new URLSearchParams(location.search);
            params.set("page", pageNum); // 페이지는 새로 덮어쓰기
            return params.toString();
        };

        // 1번 페이지로(첫 번째 블록이 아닐 경우)
        if (paging.currentBlock > 1) {
            $paging.append(`
                <li class="page_btn">
                    <a class="page_link first icon" href="${path}?${makeParams(1)}">
                        <iconify-icon icon="mage:double-arrow-left"></iconify-icon>
                    </a>
                </li>
            `);
        }

        // 이전 페이지로(첫 번째 페이지가 아닐 경우)
        if (paging.totalBlocks > 1 && !paging.first) {
            $paging.append(`
                <li class="page_btn">
                    <a class="page_link prev icon" href="${path}?${makeParams(paging.prevPage)}">
                        <iconify-icon icon="mage:arrow-left"></iconify-icon>
                    </a>
                </li>
            `);
        }

        // 페이지 버튼
        for (let num = paging.beginPage; num <= paging.endPage; num++) {
            if (paging.currentPage !== num) {
                // 현재 페이지가 아닌 버튼
                $paging.append(`
                    <li class="page_btn">
                        <a class="page_link" href="${path}?${makeParams(num)}">${num}</a>
                    </li>
                `);
            } else {
                // 현재 페이지인 버튼
                $paging.append(`
                    <li class="page_btn">
                        <span class="current">${num}</span>
                    </li>
                `);
            }
        }

        // 다음 페이지로(마지막 페이지가 아닐 경우)
        if (paging.totalBlocks > 1 && !paging.last) {
            $paging.append(`
                <li class="page_btn">
                    <a class="page_link next icon" href="${path}?${makeParams(paging.nextPage)}">
                        <iconify-icon icon="mage:arrow-right"></iconify-icon>
                    </a>
                </li>
            `);
        }

        // 마지막 페이지로(마지막 블록이 아닐 경우)
        if (paging.currentBlock < paging.totalBlocks) {
            $paging.append(`
                <li class="page_btn">
                    <a class="page_link last icon" href="${path}?${makeParams(paging.totalPages)}">
                        <iconify-icon icon="mage:double-arrow-right"></iconify-icon>
                    </a>
                </li>
            `);
        }

        $paginationElement.html($paging);
    } else {
        // paging이 없거나 totalPages가 0일 경우 페이지네이션 제거
        $parentElement.find(".pagination").remove();
    }
}