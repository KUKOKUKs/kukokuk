package com.kukokuk.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@Alias("DictEntry")
public class DictEntry {

    private int entryNo;
    private String word;
    private String definition;
    private String wordLevel;      // ENUM('고급','중급','초급','없음')
    private String usageExample;
    private String origin;         // '고유어' 또는 '한자어'
}
