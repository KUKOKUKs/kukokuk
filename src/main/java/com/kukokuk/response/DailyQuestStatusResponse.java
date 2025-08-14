package com.kukokuk.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

/*
    사용자의 경험치 이력을 활용한 일일도전과제와
    진행도/보상획득 여부값을 담을 객체
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Alias("DailyQuestStatusResponse")
public class DailyQuestStatusResponse {

    private int dailyQuestNo;           // 일일도전과제 번호
    private String contentType;         // ENUM("QUIZ", "DICTATION", "TWENTY", "STUDY", "ESSAY")
    private String contentText;         // 컨텐츠 타이틀
    private Integer point;              // 경험치 목표 퀘스트에서만 사용
    private Integer count;              // 횟수 목표 퀘스트에서만 사용
    private Integer progressValue;      // 사용자별 진행도(횟수/경험치 합)

    private Integer dailyQuestUserNo;   // 일일도전과제 완료 번호(일일도전과제 완료 테이블에 이력이 없다면 미완료로 null)
    private String isObtained;          // 사용자별 보상 수령 여부 ("Y", "N")(일일도전과제 완료 테이블에 이력이 없다면 미완료로 null)

    // 횟수를 목표로한 퀘스트인지 여부
    public boolean isCountType() {
        return count != null && count > 0 && point == null;
    }

    // 경험치량을 목표로한 퀘스트인지 여부
    public boolean isExpType() {
        return point != null && point > 0 && count == null;
    }

}
