/**
 * 북마크 토글 + 더보기/접기 (카드형)
 */
$(document).ready(function() {

    // ------------------------
    // 북마크 버튼 토글
    // ------------------------
    $(document).on('click', '.bookmark_btn', function() {
        const $btn = $(this);
        const quizNo = $btn.data('quiz-no');

        if (!quizNo || quizNo === 'undefined') {
            alert('퀴즈 정보를 찾을 수 없습니다.');
            return;
        }

        const isBookmarked = $btn.data('bookmarked') === 'Y';
        const confirmMsg = isBookmarked
            ? "이 문제를 북마크에서 삭제하시겠습니까?"
            : "이 문제를 북마크에 추가하시겠습니까?";

        if (!confirm(confirmMsg)) return;

        const url = `/api/quiz/bookmark/${quizNo}`;
        const method = isBookmarked ? 'DELETE' : 'POST';

        // Optimistic UI
        toggleBookmarkBtn($btn, !isBookmarked);

        $.ajax({
            url: url,
            method: method,
            dataType: 'json'
        })
        .done(function(data) {
            if (!data.success) {
                toggleBookmarkBtn($btn, isBookmarked); // 복구
                alert(data.message || '북마크 처리 실패');
                return;
            }

            console.log('북마크 처리 성공:', data.message);

            if (isBookmarked) {
                // 북마크 해제 시 카드 삭제 (북마크 페이지에서만)
                const $card = $btn.closest('.quiz_card');
                if ($card.length) {
                    $card.remove();

                    // 모든 카드 삭제 시 메시지 표시
                    if ($('.quiz_card').length === 0) {
                        $('.bookmark_empty_msg').text('아직 북마크한 퀴즈가 없습니다.');
                    }
                }
            }
        })
        .fail(function(xhr) {
            console.error("북마크 처리 오류:", xhr);
            toggleBookmarkBtn($btn, isBookmarked); // 복구

            let errorMsg = '북마크 처리에 실패했습니다.';
            switch(xhr.status) {
                case 404:
                    errorMsg = '퀴즈를 찾을 수 없습니다.';
                    break;
                case 403:
                    errorMsg = '권한이 없습니다. 로그인을 확인해주세요.';
                    break;
                case 500:
                    errorMsg = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
                    break;
                case 0:
                    errorMsg = '네트워크 연결을 확인해주세요.';
                    break;
            }
            alert(errorMsg);
        });
    });

    // ------------------------
    // 보기 더보기/접기
    // ------------------------
    $(document).on('click', '.toggle_btn', function() {
        const $btn = $(this);
        const $options = $btn.prev('.quiz_options');

        $options.toggleClass('hidden');
        $btn.text($options.hasClass('hidden') ? '더보기' : '접기');
    });

    // ------------------------
    // 북마크 버튼 상태 토글 함수
    // ------------------------
    function toggleBookmarkBtn($btn, isBookmarked) {
        $btn.data('bookmarked', isBookmarked ? 'Y' : 'N');

        const $icon = $btn.find('iconify-icon');
        if ($icon.length) {
            if (isBookmarked) {
                $icon.removeClass('color_border').addClass('color_yellow');
            } else {
                $icon.removeClass('color_yellow').addClass('color_border');
            }
        }

        // 툴팁 업데이트
        const label = isBookmarked ? '북마크 제거' : '북마크 추가';
        $btn.attr('data-label', label);
    }

});