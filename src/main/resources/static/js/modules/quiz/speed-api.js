/**
 * 스피드 퀴즈 10개 조회 API 요청
 * @returns {Promise<Array>} 퀴즈 배열
 */
export async function getSpeedQuizList() {
  console.log("[API] getSpeedQuizList 실행");

  const response = await $.ajax({
    method: "GET",
    url: "/api/quiz/speed",
    dataType: "json"
  });

  return response;
}

/**
 * 스피드 퀴즈 결과 저장 API 요청
 * @param {Object} payload - userNo, totalTimeSec, results 포함 객체
 * @returns {Promise<number>} 세션 번호
 */
export async function insertSpeedQuizResult(payload) {
  console.log("[API] insertSpeedQuizResult 실행", payload);

  const response = await $.ajax({
    method: "POST",
    url: "/api/quiz/result",
    contentType: "application/json",
    data: JSON.stringify(payload)
  });

  return response;
}
