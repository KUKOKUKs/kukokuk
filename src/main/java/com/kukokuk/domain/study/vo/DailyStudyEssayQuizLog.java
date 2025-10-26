package com.kukokuk.domain.study.vo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.domain.study.dto.FeedbackSectionDto;
import com.kukokuk.domain.user.vo.User;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.type.Alias;

@Log4j2
@Getter
@Setter
@Alias("DailyStudyEssayQuizLog")
public class DailyStudyEssayQuizLog {

    private int dailyStudyEssayQuizLogNo;
    private String userAnswer;
    private int score;
    private String aiFeedback;
    private Date createdDate;
    private Date updatedDate;
    private int dailyStudyEssayQuizNo;
    private int userNo;

    private DailyStudyEssayQuiz dailyStudyEssayQuiz;
    private User user;

    /**
     * JSON 문자열(aiFeedback)을 FeedbackSection 리스트로 변환하는 게터
     * - ObjectMapper를 사용해 JSON 파싱
     * - null이거나 비어있을 경우 빈 리스트 반환
     * - Thymeleaf나 서비스 레이어에서 바로 활용 가능
     */
    public List<FeedbackSectionDto> getFeedbackSections() {
        if (aiFeedback == null || aiFeedback.isBlank()) return List.of();

        try {
            ObjectMapper mapper = new ObjectMapper();
            // aiFeedback이 {"sections":[{...}]} 형태라고 가정
            var root = mapper.readTree(aiFeedback);
            var sectionsNode = root.get("sections");
            if (sectionsNode == null || !sectionsNode.isArray()) return List.of();
            return mapper.convertValue(sectionsNode, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("피드백 JSON 변환 중 오류 발생: {}", e.getMessage());
            return List.of();
        }
    }
}