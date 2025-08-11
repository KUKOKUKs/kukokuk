document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.bookmark-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const quizNo = this.dataset.quizNo;
            let isBookmarked = this.dataset.bookmarked === "true";
            const url = isBookmarked
                ? '/api/quiz/bookmark/remove'
                : '/api/quiz/bookmark/add';

            // UI 즉시 변경
            toggleBookmarkBtn(this, !isBookmarked);

            fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ quizNo: quizNo })
            })
            .then(res => res.json())
            .then(data => {
                if (!data.success) {
                    // 실패 → UI 복구
                    toggleBookmarkBtn(this, isBookmarked);
                    alert('북마크 처리 실패');
                } else {
                    // 해제 성공 시 해당 행 삭제
                    if (isBookmarked) {
                        const row = this.closest('tr');
                        row.remove();

                        // 목록이 비었으면 메시지 표시
                        if (document.querySelectorAll("tbody tr").length === 0) {
                            document.querySelector("table").remove();
                            const msg = document.createElement("span");
                            msg.textContent = "아직 북마크한 퀴즈가 없습니다.";
                            document.querySelector(".bookmark-empty-msg").appendChild(msg);
                        }
                    }
                }
            })
            .catch(() => {
                toggleBookmarkBtn(this, isBookmarked);
                alert('네트워크 오류로 북마크 처리에 실패했습니다.');
            });
        });
    });

    function toggleBookmarkBtn(btn, isBookmarked) {
        btn.dataset.bookmarked = isBookmarked;
        if (isBookmarked) {
            btn.innerHTML = '<span style="color: #e74c3c;">&#10084;</span>'; // 빨간 하트
        } else {
            btn.innerHTML = '<span style="color: #b2bec3;">&#9825;</span>'; // 빈 하트
        }
    }
});
