package com.kukokuk.domain.study.vo;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@Alias("MaterialParseJob")
public class MaterialParseJob {

    private int materialParseJobNo;
    private String url;
    private String status;
    private String message;
    private int dailyStudyMaterialNo;
    private Date createdDate;
    private Date updatedDate;

    private DailyStudyMaterial dailyStudyMaterial;
}
