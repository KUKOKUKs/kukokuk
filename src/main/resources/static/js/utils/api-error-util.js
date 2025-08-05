/**
 * 비동기 요청에 대한 에러 처리 유틸 함수
 * @param responseJSON 응답받은 에러처리 내용
 */
export function apiErrorProcessByXhr(responseJSON) {
    const success = responseJSON.success;    // 요청 처리 성공 여부
    const status = responseJSON.status;      // 에러 코드
    const message = responseJSON.message ?? "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";    // 에러 메세지

    if (!success) {
        if (status >= 400 && status < 600) {
            // 400~600 에러 코드 처리
            // 4xx: 클라이언트 오류 (입력값 오류, 인증 실패 등)
            // 5xx: 서버 오류 (예외처리 등)
            alert(message);
        } else {
            alert(`알 수 없는 오류가 발생했습니다.\n에러코드: ${status})\n페이지를 새로고침합니다.`);
            location.reload();
        }
    }
}