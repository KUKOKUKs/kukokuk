/**
 * 북마크 토글 + 더보기/접기 (카드형)
 */
document.addEventListener('DOMContentLoaded', function() {
    // 북마크 토글
    document.querySelectorAll('.bookmark-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const quizNo = this.dataset.quizNo;
            let isBookmarked = this.dataset.bookmarked === "true";
            const url = `/api/quiz/bookmark/${quizNo}`;
            const method = isBookmarked ? 'DELETE' : 'POST';

            toggleBookmarkBtn(this, !isBookmarked);

            fetch(url, { method: method })
            .then(res => res.json())
            .then(data => {
                if (!data.success) {
                    toggleBookmarkBtn(this, isBookmarked);
                    alert(data.message || '북마크 처리 실패');
                    return;
                }
                if (isBookmarked) {
                    this.closest('.quiz-card').remove();
                    if (document.querySelectorAll('.quiz-card').length === 0) {
                        document.querySelector('.bookmark-empty-msg').textContent =
                            '아직 북마크한 퀴즈가 없습니다.';
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
        btn.innerHTML = isBookmarked
            ? '<span style="color: #e74c3c;">&#10084;</span>'
            : '<span style="color: #b2bec3;">&#9825;</span>';
    }

    // 더보기/접기
    document.querySelectorAll('.toggle-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const container = this.closest('.text-toggle');
            container.classList.toggle('expanded');
            this.textContent = container.classList.contains('expanded') ? '접기' : '더보기';
        });
    });
});
