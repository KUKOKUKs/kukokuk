/**
 * 연, 월, 일 값을 전달받아 유효한 날짜인지 확인
 * @param y 연도
 * @param m 월
 * @param d 일
 * @returns {boolean} 유효한 날짜 여부
 */
export function validateDate(y, m, d) {
    const date = new Date(`${y}-${m}-${d}`);
    return (
        date.getFullYear() === parseInt(y, 10) &&
        date.getMonth() + 1 === parseInt(m, 10) &&
        date.getDate() === parseInt(d, 10)
    );
}