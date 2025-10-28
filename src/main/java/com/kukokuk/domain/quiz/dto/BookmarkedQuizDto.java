package com.kukokuk.domain.quiz.dto;

import com.kukokuk.common.util.QuizUtil;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("BookmarkedQuizDto")
public class BookmarkedQuizDto {

    private int bookmarkNo;
    private int quizNo;
    private Date createdDate;           // 북마크 날짜

    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private int successAnswer;          // 문제에 대한 답 번호

    private String questionType;        // '뜻' | '단어'
    private String difficulty;          // '쉬움' | '보통' | '어려움'
    private Integer usageCount;
    private Integer successCount;
    private Double accuracyRate;

    private Integer resultNo;           // 사용자가 푼 문제일 경우 이력 번호
    private Integer sessionNo;          // 해당 문제가 포함된 묶음 번호
    private Integer selectedChoice;     // 사용자가 선택한 보기번호
    private String isSuccess;           // 정답 여부
    private Date resultDate;            // 문제 푼 이력 날짜

    /**
     * 누적 정답률을 계산하는 커스텀 게터
     * DB에 저장된 accuracyRate 대신 계산값을 반환
     * @return 정답률 소수점 둘째자리까지 반올림
     */
    public double getCalculatedAccuracyRate() {
        // usageCount가 0이거나 null이면 0%로 처리
        if (usageCount == null || usageCount == 0) return 0.0;

        // successCount가 null이면 0으로 계산
        int success = (successCount == null) ? 0 : successCount;

        // 정답률 계산
        double rate = ((double) success / usageCount) * 100;

        // 소수점 둘째 자리까지 반올림
        return Math.round(rate * 100.0) / 100.0;
    }

    // 보기들을 한번에 꺼내 사용할 수 있도록 하는 게터
    public List<String> getOptions() {
        return QuizUtil.extractOptions(option1, option2, option3, option4);
    }


}
