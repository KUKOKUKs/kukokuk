/**
 * 보기 선택 시 선택된 보기 번호 반환
 * @param {jQuery} $button - 클릭된 버튼 요소
 * @returns {number} 선택된 보기 번호
 */
export function handleOptionSelect($button) {
    console.log("handleOptionSelect() 실행");

    const choiceNumber = Number($button.data("choice"));

    // UI 업데이트
    $("#quiz-options").find(".option_btns").removeClass("selected");
    $button.addClass("selected");

    console.log(`보기 ${choiceNumber} 선택`);
    return choiceNumber;
}

/**
 * 힌트로 제거된 보기 UI 처리
 * @param {number} optionNumber - 제거할 보기 번호
 */
export function handleHintRemoveOption(optionNumber) {
    console.log("handleHintRemoveOption() 실행");

    const $optionBtn = $(`#quiz-options button[data-choice="${optionNumber}"]`);

    // 버튼 비활성화
    $optionBtn
    .addClass("hint-removed")
    .prop("disabled", true)
    .off("click");

    // 시각적 효과
    $optionBtn.find(".option").css({
        "text-decoration": "line-through",
        "opacity": "0.5",
        "color": "#999"
    });

    // 선택 해제
    if ($optionBtn.hasClass("selected")) {
        $optionBtn.removeClass("selected");
        return true; // 선택이 해제됨
    }

    return false; // 선택 해제 안됨
}

/**
 * 힌트 카운트 애니메이션 효과
 * @param {jQuery} $element - 힌트 카운트 요소
 */
export function animateHintCount($element) {
    $element.addClass("action");
    setTimeout(() => $element.removeClass("action"), 200);
}