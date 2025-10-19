export const regExEmail = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z.]{2,5}$/; // 이메일 정규표현식
export const regExPassword = /^(?=.*[a-zA-Z])(?=.*[@$!%*?&])[a-zA-Z\d@$!%*?&]{8,16}$/; // 비밀번호 정규표현식
export const regExNickname = /^[a-zA-Z0-9가-힣_]{4,16}$/; // 닉네임 정규표현식

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

/**
 * 생년월일 유효성 검증
 * @param birthDate 생년월일
 * @returns boolean - 유효한 생년월일 날짜 여부
 */
export function validateBirthDate(birthDate) {
    let isValid = false;

    const digitsOnly = birthDate.replace(/\D/g, ''); // 숫자만 추출
    const current = digitsOnly.slice(0, 8); // 최대 8자리까지 유지

    // 값이 비었거나 8자리 이하일 경우
    if (current !== "" || !current.length < 8) {
        const y = current.slice(0, 4);
        const m = current.slice(4, 6);
        const d = current.slice(6, 8);

        // 오늘 이전 날짜인지 확인
        if (validateDate(y, m, d)) {
            const inputDate = new Date(`${y}-${m}-${d}`);
            const today = new Date();
            today.setHours(0, 0, 0, 0); // 시간을 0으로 설정하여 비교 안정성 확보
            isValid = inputDate < today;
        }
    }

    return isValid;
}