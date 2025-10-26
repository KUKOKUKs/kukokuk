package com.kukokuk.domain.quiz.vo;

import com.kukokuk.common.util.QuizUtil;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@Alias("QuizMaster")
public class QuizMaster {

    private int quizNo;
    private int entryNo;
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private float accuracyRate;  // 정답률 (%)
    private int successAnswer;
    private String questionType;   // ENUM('단어','뜻')
    private String difficulty;     // ENUM('어려움','보통','쉬움')
    private int usageCount;
    private int successCount;
    private Date createdDate; // 생성일
    private Date updatedDate; // 수정일

    // 보기들을 한번에 꺼내 사용할 수 있도록 하는 게터
    public List<String> getOptions() {
        return QuizUtil.extractOptions(option1, option2, option3, option4);
    }

}
