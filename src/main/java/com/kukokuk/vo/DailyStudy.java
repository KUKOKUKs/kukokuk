package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyStudy")
public class DailyStudy {

    private int dailyStudyNo;
    private int dailyStudyMaterialNo;
    private String title;
    private int studyDifficulty;
    private int cardCount;
    private Date createdDate;
    private Date updatedDate;

    private DailyStudyMaterial dailyStudyMaterial;
}
