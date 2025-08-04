$(document).ready(() => {
    // 로그아웃 폼
    const $logoutForm = $('#logout-form');
    
    // 폼 제출 이벤트 발생 시 유효성 검사 후 제출
    $logoutForm.submit(function (e) {
        e.preventDefault();

        const isConfirm = confirm('로그아웃하시겟습니까?');

        // 취소 시 리턴
        if (!isConfirm) return false;

        this.submit();
    });

    // 사용자 진도/단계 선택 모달창 열기
    const $modalStudyLevelBtn = $("#modal-study-level-btn"); // 모달창 열기 버튼
    const $modalStudyLevel = $("#modal-study-level"); // 모달창
    $modalStudyLevelBtn.click(function () {
        if ($modalStudyLevel.length) {
            // 해당 모달창 요소가 있을 경우 열기
            $modalStudyLevel.show();
        }
    });

    // 모달창 닫기
    const $modalCloseBtns = $(".modal_close"); // 모달창 닫기 버튼
    const $modalAll = $(".modal_wrap"); // 전체 모달창
    $modalCloseBtns.click(function () {
        if ($modalAll.length) {
            // 모달 내부 폼 요소 초기화
            $modalAll.find("form").each(function () {
                $(this).find("button[type='submit']").addClass("disabled");
                this.reset();
            });
            // 모달창 닫기
            $modalAll.hide();
        }
    });
});