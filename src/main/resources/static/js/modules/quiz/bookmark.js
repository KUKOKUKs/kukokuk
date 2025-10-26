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
});
