package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DictationQuestionLog")
public class DictationQuestionLog {

  private int dictationQuestionLogNo;  // 식별자
  private int userNo;                  // 회원번호
  private int dictationQuestionNo;     // 문제 번호
  private int dictationSessionNo;      // 문제 세트 번호
  private String tryCount;             // ENUM('0','1','2') : 문자열로 처리
  private String isSuccess;            // ENUM('Y','N')
  private String usedHint;             // ENUM('Y','N'), default 'N'
  private Date createdDate;            // 생성일 (NOW())

}

