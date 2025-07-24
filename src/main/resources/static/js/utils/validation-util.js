/**
 * 연, 월, 일 값을 전달받아 유효한 날짜인지 확인
 * @param y 연도
 * @param m 월
 * @param d 일
 * @returns {boolean} 유효한 날짜 여부
 */
export function validateDate(y, m, d) {
    // 전달받은 값으로 Date객체 생성
    // 유효하지 않은 값으로 객체 생성 시 자동으로 유효한 날짜로 생성
    const date = new Date(`${y}-${m}-${d}`);
    
    // 전달받은 값과 생성된 날짜의 값이 다를 경우 유효한 날짜가 아님을 판단
    return (
        date.getFullYear() === parseInt(y, 10) &&
        date.getMonth() + 1 === parseInt(m, 10) &&
        date.getDate() === parseInt(d, 10)
    );
}