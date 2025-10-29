package com.kukokuk.domain.dictation.vo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DictationSession")
public class DictationSession {

  private int dictationSessionNo; // 문제 세트 번호
  private int userNo;             // 회원 번호
  private Date startDate;         // 시작 시각
  private Date endDate;           // 종료 시각
  private int correctScore;       // 정답 점수
  private int correctCount;       // 맞은 개수
  private int hintUsedCount;      // 사용한 힌트 수
  private Date createdDate;        // 생성일 (NOW())
  private BigDecimal absoluteScore;

  // totalTimeSec 커스텀 Getter
  public Double getTotalTimeSec() {
    if (startDate == null || endDate == null) {
      return null;
    }
    long ts = Math.max(0L, endDate.getTime() - startDate.getTime()); // 음수 방지
    return ts / 1000.0; // 12345(ms) -> 12.345(s)
  }
}
