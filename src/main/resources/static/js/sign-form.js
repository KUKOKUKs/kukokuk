// sign form 내부 아이콘 버튼으로 해당하는 input 값 초기화
$(".input_delete_btn").click(function () {
    // 클릭한 요소의 가장 가까운 .input_info 요소의 자식요소 input 찾기
    const $input = $(this).closest(".input_info").find("input");
    $input.val(""); // 값 초기화
    $input.focus(); // 다시 포커스 유지
});

// sign form 내부 패스워드 보기 버튼으로 해당하는 input type 토글
$("#input-password-view-btn").click(function () {
    // 클릭한 요소의 가장 가까운 .input_info 요소의 자식요소 input 찾기
    const $passwordInput = $(this).closest(".input_info").find("input");
    const inputType = $passwordInput.attr("type");  // 인풋 타입 가져오기
    const isPassword = inputType === "password";
    
    // 인풋 타입 토글
    $passwordInput.attr("type", isPassword ? "text" : "password");

    // 클릭한 요소의 아이콘 토글
    const $icon = $(this).find("iconify-icon");
    $icon.attr("icon", isPassword ? "clarity:eye-show-solid" : "clarity:eye-hide-solid");
});