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
  console.log("퀴즈 응답 확인:", response);
  return response;
}
