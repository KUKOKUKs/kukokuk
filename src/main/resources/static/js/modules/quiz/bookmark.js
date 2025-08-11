/**
 * 북마크 토글 + 더보기/접기 공용 스크립트
 * - 목록 페이지, 상세보기 페이지 모두 사용 가능
 * - 버튼: .bookmark-btn
 * - data-bookmarked="true|false"
 * - data-quiz-no="{quizNo}"
 */
document.addEventListener('DOMContentLoaded', function() {
    // ------------------------
    // 북마크 토글 기능
    // ------------------------
    document.querySelectorAll('.bookmark-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const quizNo = this.dataset.quizNo;
            let isBookmarked = this.dataset.bookmarked === "true";

            const url = `/api/quiz/bookmark/${quizNo}`;
            const method = isBookmarked ? 'DELETE' : 'POST';

            // optimistic update
            toggleBookmarkBtn(this, !isBookmarked);

            fetch(url, { method: method })
            .then(res => res.json())
            .then(data => {
                if (!data.success) {
                    toggleBookmarkBtn(this, isBookmarked);
                    alert(data.message || '북마크 처리 실패');
                    return;
                }

                // 북마크 해제 후 목록 페이지일 경우, 행 삭제
                if (isBookmarked && this.closest('tr')) {
                    const row = this.closest('tr');
                    row.remove();
                    // 목록 비었으면 안내 메시지 표시
                    if (document.querySelectorAll("tbody tr").length === 0) {
                        const table = document.querySelector("table");
                        if (table) table.remove();
                        const msgContainer = document.querySelector(".bookmark-empty-msg");
                        if (msgContainer) {
                            const msg = document.createElement("span");
                            msg.textContent = "아직 북마크한 퀴즈가 없습니다.";
                            msgContainer.appendChild(msg);
                        }
                    }
                }

                console.log(data.message); // 성공 메시지 로그
            })
            .catch(() => {
                toggleBookmarkBtn(this, isBookmarked);
                alert('네트워크 오류로 북마크 처리에 실패했습니다.');
            });
        });
    });

    /**
     * 하트 버튼 UI 토글
     */
    function toggleBookmarkBtn(btn, isBookmarked) {
        btn.dataset.bookmarked = isBookmarked;
        btn.innerHTML = isBookmarked
            ? '<span style="color: #e74c3c;">&#10084;</span>'
            : '<span style="color: #b2bec3;">&#9825;</span>';
    }

    // ------------------------
    // 더보기/접기 기능
    // ------------------------
    document.querySelectorAll('.toggle-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const container = this.closest('.text-toggle');
            container.classList.toggle('expanded');
            this.textContent = container.classList.contains('expanded') ? '접기' : '더보기';
        });
    });
});
