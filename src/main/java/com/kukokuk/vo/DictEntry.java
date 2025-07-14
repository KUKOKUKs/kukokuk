package com.kukokuk.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DictEntry") // Mapper XML에서 이 이름으로 사용 가능
public class DictEntry {
  private int entryNo;
  private String word;
  private String definition;
  private String partOfSpeech;
  private String originJson;
  private String dictLink;
  private String createdDate;   
  private String updatedDate;
}
