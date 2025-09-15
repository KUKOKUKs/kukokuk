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
    
    // 네이바 토글
    const $navContainer = $('#nav-container'); // 네비 컨테이너 요소
    const $navToggleBtn = $("#nav-toggle-btn"); // 네비 토글 버튼
    $navToggleBtn.click(function () {
        if ($navContainer.length) {
            $navContainer.toggleClass("closed");
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
            $modalAll.hide().removeClass("open");
        }
    });

    // 탭 버튼, 탭 컨텐츠 핸들러
    const $tabBtns = $(".tab_btn_list .tab_btn"); // 탭 버튼
    const $tabContents = $(".tab_content"); // 탭 컨텐츠
    $tabBtns.click(function () {
        if ($modalAll.length) {
            const $this = $(this);
            const index = $this.index(); // 클릭한 요소의 index

            $tabBtns.removeClass("selected_left selected"); // 선택된 요소의 앞 요소에 추가된 클라스 제거
            $tabContents.removeClass("selected"); // 선택된 탭 컨텐츠 요소 클라스 제거

            if (index > 0) {
                // 첫번째 요소가 아닐 경우 선택된 요소의 앞 요소에 클라스 추가
                $tabBtns.eq(index - 1).addClass("selected_left");
            }

            // 선택한 탭 버튼 활성화
            $(this).addClass("selected");
            // 선택한 탭 컨텐츠 활성화
            $tabContents.eq(index).addClass("selected").siblings().removeClass("selected");
        }
    });
    
    // 정답 확인 버튼 핸들러
    const $checkCorrect = $(".check_correct"); // 정답 확인 버튼
    $checkCorrect.click(function () {
        const $this = $(this);
        
        // 버튼에 클라스 추가 및 자식 요소 텍스트 전부 보이도록 클래스 제거
        $this.addClass("checked")
            .find(".correct_answer").removeClass("text_ellipsis");
    });
});