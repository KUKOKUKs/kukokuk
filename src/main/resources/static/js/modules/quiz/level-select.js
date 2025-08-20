document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("levelQuizForm");

    form.addEventListener("submit", function (e) {
        const difficultyChecked = form.querySelector('input[name="difficulty"]:checked');
        const typeChecked = form.querySelector('input[name="questionType"]:checked');

        if (!difficultyChecked) {
            alert("난이도를 선택해주세요!");
            e.preventDefault();
            return;
        }
        if (!typeChecked) {
            alert("출제 유형을 선택해주세요!");
            e.preventDefault();
        }
    });
});
