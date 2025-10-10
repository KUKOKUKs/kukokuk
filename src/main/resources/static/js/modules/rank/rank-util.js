/* rank에서만 사용될 유틸 함수 */

/**
 * Date 객체를 "yyyy-MM" 형식으로 변환하는 유틸 함수
 * @param {Date} date 변환할 Date 객체
 * @returns {string} yyyy-MM 형식의 문자열 (예: "2025-10")
 */
export function formatDateToMonth(date) {
    // 연도 추출 (예: 2025)
    const year = date.getFullYear();

    // 월 추출 후 0부터 시작하므로 +1 padStart로 두 자리 보정 (예: 9 -> "09")
    const month = String(date.getMonth() + 1).padStart(2, '0');

    // yyyy-MM 문자열로 결합
    return `${year}-${month}`;
}