import {apiToggleBookmark} from "./quiz-api.js";

$(document).ready(function () {
    // 북마크 관련
    // 북마크 토글 (비동기 요청)
    $(document).on("click", ".bookmark_btn", async function() {
        const $this = $(this);
        const $bookmardBtns = $(".bookmark_btn");
        const quizNo = $this.attr('data-quiz-no');
        const isBookmarked = $this.attr("data-bookmarked") === "true";
        const $bookmarkIcon = $this.find(".bookmard_icon");

        $bookmardBtns.addClass("disabled"); // 중복/다중 클릭 방지

        try {
            await apiToggleBookmark(quizNo, isBookmarked);
            if (!isBookmarked) {
                $this.attr("data-bookmarked", "true").attr("data-label", "북마크 제거");
                $bookmarkIcon.removeClass("color_border").addClass("color_yellow");
            } else {
                $this.attr("data-bookmarked", "false").attr("data-label", "북마크 추가");
                $bookmarkIcon.removeClass("color_yellow").addClass("color_border");
            }
        } catch (error) {
            console.error("북마크 변경 요청 실패: ", error.message);
            alert("북마크 변경 요청에 실패하였습니다.\n다시 시도해 주세요.");
            // location.reload(); // 새로 고침
        } finally {
            $bookmardBtns.removeClass("disabled");
        }
    });
    
    // 북마크된 문제 풀이(연습용 DB저장하지 않음)
    $(document).on("click", ".answer_check_btn", function () {
        const $this = $(this); // 클릭한 정답 확인 버튼
        const bookmarkNo = $this.attr('data-bookmark-no'); // 북마크 번호
        const successAnswer = $this.attr('data-correct-check'); // 정답 번호
        const $question = $(`.question_${bookmarkNo}`); // 문제 요소
        const $selectedOption = $(`input:radio[name="selectedOption${bookmarkNo}"]:checked`); // 선택한 보기 요소
        const $selectedOptionParent = $selectedOption.closest(".bookmark_options"); // 해당 문제의 보기 리스트 부모 요소

        // 유효성 검사
        if (!$selectedOption.is(":checked")) {
            alert("보기를 선택해 주세요.");
            return false;
        }

        const value = $selectedOption.val(); // 선택한 보기 번호
        const isSuccess = successAnswer === value; // 정답 확인
        const resutlIconHtml = `<span class="result_icon icon large ${isSuccess ? 'correct' : 'incorrect'}"></span>`;

        console.log("bookmarkNo: ", bookmarkNo);
        console.log("successAnswer: ", successAnswer);
        console.log("selectedOption: ", value);
        console.log("isSuccess: ", isSuccess);
        
        // 정답일 경우
        if (isSuccess) {
            $selectedOptionParent.children(".list_option").addClass("disabled"); // 전체 disabled
            $selectedOption.parents(".list_option").removeClass("disabled"); // 정답인 보기 disabled 제거
        }

        // 결과 아이콘 표시
        $question.find(".result_icon").remove(); // 기존 아이콘 제거
        $question.append(resutlIconHtml); // 결과 아이콘 추가
    });
});
