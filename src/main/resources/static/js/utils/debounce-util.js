/**
 * 전달된 함수를 디바운싱하여 반환합니다.
 * 반환된 함수는 호출될 때 인자를 그대로 전달받아 실행되며, 반환값은 없습니다.
 *
 * @param {Function} fn - 디바운싱할 함수
 * @param {number} [delay=300] - 지연 시간 (밀리초)
 * @returns {Function} 디바운싱된 함수 (반환값 없음)
 */
export function debounce(fn, delay = 300) {
    // 타이머 ID를 저장할 변수 클로저로 유지됨
    // 내부에서 반환된 함수가 계속 참조하고 있기 때문에 메모리에 유지
    let timer;

    // 호출 시 디바운싱 로직을 적용할 함수 반환
    return function (...args) {
        // 이전에 설정된 타이머가 있다면 취소
        clearTimeout(timer);

        // 새 타이머를 설정하여 delay 후 fn 함수 실행 예약
        timer = setTimeout(() => {
            // 현재 this 컨텍스트와 전달된 인자 그대로 fn 실행
            fn.apply(this, args);
        }, delay);
    };
}