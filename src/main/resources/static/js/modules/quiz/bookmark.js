/**
 * 북마크 토글 + 더보기/접기 (카드형)
 */
document.addEventListener('DOMContentLoaded', function() {

    // ------------------------
    // 북마크 버튼 토글
    // ------------------------
    document.querySelectorAll('.bookmark_btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const quizNo = this.dataset.quizNo;
            let isBookmarked = (this.dataset.bookmarked || "").toLowerCase() === "true";

            const confirmMsg = isBookmarked
                ? "이 문제를 북마크에서 삭제하시겠습니까?"
                : "이 문제를 북마크에 추가하시겠습니까?";
            if (!confirm(confirmMsg)) return;

            const url = `/api/quiz/bookmark/${quizNo}`;
            const method = isBookmarked ? 'DELETE' : 'POST';

            // optimistic UI
            toggle_bookmark_btn(this, !isBookmarked);

            fetch(url, { method: method })
            .then(res => res.json())
            .then(data => {
                if (!data.success) {
                    toggle_bookmark_btn(this, isBookmarked); // 복구
                    alert(data.message || '북마크 처리 실패');
                    return;
                }
                if (isBookmarked) {
                    // 북마크 해제 시 카드 삭제
                    const card = this.closest('.quiz_card');
                    if (card) card.remove();

                    // 모든 카드 삭제 시 메시지 표시
                    if (document.querySelectorAll('.quiz_card').length === 0) {
                        const emptyMsg = document.querySelector('.bookmark_empty_msg');
                        if (emptyMsg) emptyMsg.textContent = '아직 북마크한 퀴즈가 없습니다.';
                    }
                }
            })
            .catch(err => {
                console.error("fetch 오류:", err);
                toggle_bookmark_btn(this, isBookmarked); // 복구
                alert('네트워크 오류로 북마크 처리에 실패했습니다.');
            });
        });
    });

    // ------------------------
    // 보기 더보기/접기
    // ------------------------
    document.querySelectorAll('.toggle_btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const options = this.previousElementSibling; // quiz_options
            options.classList.toggle('hidden');
            this.textContent = options.classList.contains('hidden') ? '더보기' : '접기';
        });
    });

    // ------------------------
    // 북마크 버튼 상태 토글 함수 (Iconify 아이콘 사용)
    // ------------------------
    function toggle_bookmark_btn(btn, isBookmarked) {
        btn.dataset.bookmarked = isBookmarked ? "true" : "false";
        btn.innerHTML = isBookmarked
            ? '<iconify-icon icon="emojione:red-heart" style="font-size: 1.5rem;"></iconify-icon>'
            : '<iconify-icon icon="emojione-monotone:red-heart" style="font-size: 1.5rem;"></iconify-icon>';
    }

});
