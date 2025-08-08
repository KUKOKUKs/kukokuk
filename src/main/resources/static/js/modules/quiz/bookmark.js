document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.bookmark-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const quizNo = this.dataset.quizNo;
            let bookmarked = this.dataset.bookmarked === "true";
            const url = bookmarked
                ? '/api/quiz/bookmark/remove'
                : '/api/quiz/bookmark/add';

            // UI 즉시 반영 (optimistic update)
            toggleBookmarkBtn(this, !bookmarked);

            fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ quizNo: quizNo })
            })
            .then(res => res.json())
            .then(data => {
                if (!data.success) {
                    // 실패 시 다시 원상복구
                    toggleBookmarkBtn(this, bookmarked);
                    alert('북마크 처리 실패');
                }
            })
            .catch(() => {
                toggleBookmarkBtn(this, bookmarked);
                alert('네트워크 오류로 북마크 처리에 실패했습니다.');
            });
        });
    });

    function toggleBookmarkBtn(btn, isBookmarked) {
        btn.dataset.bookmarked = isBookmarked;
        // 하트 아이콘 즉시 변경
        if (isBookmarked) {
            btn.innerHTML = '<span style="color: #e74c3c;">&#10084;</span>';
        } else {
            btn.innerHTML = '<span style="color: #b2bec3;">&#9825;</span>';
        }
    }
});
