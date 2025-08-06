let currentIndex = 0;
let usedHint = false;

$(document).ready(function() {
    showQuiz(currentIndex);

    $("#hint-btn").click(function() {
        if (usedHint) return;
        const quiz = quizList[currentIndex];
        $.post("/api/quiz/hint", {
            quizNo: quiz.quizNo,
            correctChoice: quiz.successAnswer
        }, function(res) {
            const removeChoice = res.removeChoice;
            $(".quiz-option[data-choice='" + removeChoice + "']").hide();
            $("#hint-btn").prop("disabled", true);
            usedHint = true;
        });
    });

    $("#next-btn").click(function() {
        if (currentIndex < quizList.length - 1) {
            currentIndex++;
            usedHint = false;
            showQuiz(currentIndex);
            $("#hint-btn").prop("disabled", false);
        } else {
            // 결과 제출 로직 구현 (폼 전송 또는 Ajax)
            submitQuizResults();
        }
    });

    $(document).on("click", ".quiz-option", function() {
        $(".quiz-option").removeClass("selected");
        $(this).addClass("selected");
    });
});

function showQuiz(idx) {
    const quiz = quizList[idx];
    $("#question").text(quiz.question);
    for (let i = 1; i <= 4; i++) {
        $(".quiz-option[data-choice='" + i + "']").show().text(quiz["option" + i]).removeClass("selected");
    }
    $("#cur").text(idx + 1);
    $("#total").text(quizList.length);
    $("#hint-btn").prop("disabled", false);
}
