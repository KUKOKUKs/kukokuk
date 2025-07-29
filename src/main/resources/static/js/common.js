$(document).ready(() => {
    // 로그아웃
    const $logoutForm = $('#logout-form');
    
    // 폼 제출 이벤트 발생 시 유효성 검사 후 제출
    $logoutForm.submit(function (e) {
        e.preventDefault();

        const isConfirm = confirm('로그아웃하시겟습니까?');

        // 취소 시 리턴
        if (!isConfirm) return false;

        this.submit();
    });
});