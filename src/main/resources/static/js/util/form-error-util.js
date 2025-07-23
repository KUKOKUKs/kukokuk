/**
 * 폼 내부 인풋 타이틀 숨김 처리 및 에러 메세지 요소 추가
 * @param $inputElement 해당 인풋
 * @param message 에러 메세지
 */
export function addInputErrorMessage($inputElement, message) {
    // 해당 인풋 타이틀 숨김
    $inputElement.siblings(".input_title").addClass("disabled");
    // 해당 인풋에 에러 메세지 요소 추가
    $inputElement.after(`<span class="input_error">${message}</span>`);
}

/**
 * 폼 내부 인풋 타이틀 복구 처리 및 에러 메세지 요소 제거
 * @param $inputElement 해당 인풋
 */
export function clearInputErrorMessage($inputElement) {
    // 해당 인풋 타이틀 복구
    $inputElement.siblings(".input_title").removeClass("disabled");
    // 해당 인풋 에러 메세지 제거
    $inputElement.siblings(".input_error").remove();
}

/**
 * 폼 내부 모든 인풋 타이틀 복구 처리 및 에러 메세지 요소 제거
 * @param $form
 */
export function allClearFormErrorMessage($form) {
    // 폼 내부 인풋 타이틀 복구
    $form.find(".input_title").removeClass("disabled");
    // 폼 내부 인풋 에러 메세지 제거
    $form.find(".input_error").remove();
}