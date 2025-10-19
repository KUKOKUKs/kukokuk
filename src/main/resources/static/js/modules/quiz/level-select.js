/*
    이벤트 막는것은 분기처리마다 막도록 처리하지 않고 로직 시작전에 막고 시작함
    유효성 검증 후 직접 제출 로직 실행으로 처리(확실한 로직 구분과 명시적으로 실행하여 가독성/유지보수 향상)
 */

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("levelQuizForm"); // id 코드 컨벤션 맞지 않음

    form.addEventListener("submit", function (e) {
        const difficultyChecked = form.querySelector('input[name="difficulty"]:checked');
        const typeChecked = form.querySelector('input[name="questionType"]:checked');

        if (!typeChecked) {
            alert("출제 유형을 선택해주세요!");
            e.preventDefault();
            return;
        }

        if (!difficultyChecked) {
            alert("난이도를 선택해주세요!");
            e.preventDefault();
        }
    });
});
