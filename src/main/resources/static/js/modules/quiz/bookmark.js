document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.bookmark-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const quizNo = this.dataset.quizNo;
            const bookmarked = this.dataset.bookmarked === "true";
            const url = bookmarked ? '/api/quiz/bookmark/remove' : '/api/quiz/bookmark/add';

            fetch(url, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({quizNo: quizNo})
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    // 새로고침 또는 아이콘만 토글
                    window.location.reload(); // 간단히 새로고침. 또는 버튼 아이콘만 교체
                } else {
                    alert('북마크 처리 실패');
                }
            });
        });
    });
});
