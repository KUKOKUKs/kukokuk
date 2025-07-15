package com.kukokuk.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("DictEntry")
public class DictEntry {
  private int entryNo;
  private String word;
  private String definition;
  private String wordLevel;      // ENUM('고급','중급','초급','없음')
  private String usageExample;
  private String origin;         // '고유어' or '한자어'
}
