/**
 * 북마크 토글 + 더보기/접기 (카드형)
 *  수정: CSRF 토큰 및 에러 처리 개선
 */
document.addEventListener('DOMContentLoaded', function() {

    // CSRF 토큰 가져오기
    function getCsrfToken() {
        const metaToken = document.querySelector('meta[name="_csrf"]');
        return metaToken ? metaToken.getAttribute('content') : null;
    }

    // CSRF 헤더명 가져오기
    function getCsrfHeader() {
        const metaHeader = document.querySelector('meta[name="_csrf_header"]');
        return metaHeader ? metaHeader.getAttribute('content') : 'X-CSRF-TOKEN';
    }

    // ------------------------
    // 북마크 버튼 토글
    // ------------------------
    document.querySelectorAll('.bookmark_btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const quizNo = this.dataset.quizNo;

            // 🔧 수정: quizNo 검증 추가
            if (!quizNo || quizNo === 'undefined') {
                alert('퀴즈 정보를 찾을 수 없습니다.');
                return;
            }

            let isBookmarked = (this.dataset.bookmarked || "").toLowerCase() === "true" ||
                this.dataset.bookmarked === "Y";

            const confirmMsg = isBookmarked
                ? "이 문제를 북마크에서 삭제하시겠습니까?"
                : "이 문제를 북마크에 추가하시겠습니까?";
            if (!confirm(confirmMsg)) return;

            const url = `/api/quiz/bookmark/${quizNo}`;
            const method = isBookmarked ? 'DELETE' : 'POST';

            // 🔧 수정: CSRF 토큰 포함한 헤더 설정
            const headers = {
                'Content-Type': 'application/json'
            };

            const csrfToken = getCsrfToken();
            if (csrfToken) {
                headers[getCsrfHeader()] = csrfToken;
            }

            // optimistic UI
            toggle_bookmark_btn(this, !isBookmarked);

            fetch(url, {
                method: method,
                headers: headers
            })
            .then(res => {
                if (!res.ok) {
                    throw new Error(`HTTP ${res.status}: ${res.statusText}`);
                }
                return res.json();
            })
            .then(data => {
                if (!data.success) {
                    toggle_bookmark_btn(this, isBookmarked); // 복구
                    alert(data.message || '북마크 처리 실패');
                    return;
                }

                console.log('북마크 처리 성공:', data.message);

                if (isBookmarked) {
                    // 북마크 해제 시 카드 삭제 (북마크 페이지에서만)
                    const card = this.closest('.quiz_card');
                    if (card) {
                        card.remove();

                        // 모든 카드 삭제 시 메시지 표시
                        if (document.querySelectorAll('.quiz_card').length === 0) {
                            const emptyMsg = document.querySelector('.bookmark_empty_msg');
                            if (emptyMsg) emptyMsg.textContent = '아직 북마크한 퀴즈가 없습니다.';
                        }
                    }
                }
            })
            .catch(err => {
                console.error("북마크 처리 오류:", err);
                toggle_bookmark_btn(this, isBookmarked); // 복구

                // 수정: 더 구체적인 에러 메시지
                let errorMsg = '북마크 처리에 실패했습니다.';
                if (err.message.includes('404')) {
                    errorMsg = '퀴즈를 찾을 수 없습니다.';
                } else if (err.message.includes('403')) {
                    errorMsg = '권한이 없습니다. 로그인을 확인해주세요.';
                } else if (err.message.includes('500')) {
                    errorMsg = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
                } else if (err.name === 'TypeError' && err.message.includes('fetch')) {
                    errorMsg = '네트워크 연결을 확인해주세요.';
                }

                alert(errorMsg);
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
        // 수정: 데이터 속성 값 정규화
        btn.dataset.bookmarked = isBookmarked ? "Y" : "N";

        const icon = btn.querySelector('iconify-icon');
        if (icon) {
            if (isBookmarked) {
                icon.setAttribute('icon', 'material-symbols:bookmark-star');
                icon.className = 'icon title_font color_yellow';
            } else {
                icon.setAttribute('icon', 'material-symbols:bookmark-star');
                icon.className = 'icon title_font color_border';
            }
        }

        // 툴팁 업데이트
        const label = isBookmarked ? "북마크 제거" : "북마크 추가";
        btn.setAttribute('data-label', label);
    }

});