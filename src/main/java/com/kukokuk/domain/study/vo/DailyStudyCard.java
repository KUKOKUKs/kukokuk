package com.kukokuk.domain.study.vo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.domain.study.dto.StudyCardContent;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DailyStudyCard")
public class DailyStudyCard {

    private int dailyStudyCardNo;
    private String title;
    private String content;
    private Date createdDate;
    private Date updatedDate;
    private int cardIndex;
    private int dailyStudyNo;

    // content(JSON 문자열)를 실제 List<StudyCardContent>로 파싱하는 getter
    public List<StudyCardContent> getStudyCardContent() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of(); // 파싱 실패 시 빈 리스트 반환
        }
    }

}
