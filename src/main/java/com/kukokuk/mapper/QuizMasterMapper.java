package com.kukokuk.mapper;

import com.kukokuk.dto.QuizLevelResultDto;
import com.kukokuk.vo.QuizMaster;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 퀴즈 마스터(문제) 관련 DB 연동 Mapper
 */
@Mapper
public interface QuizMasterMapper {

    /**
     * 퀴즈의 갯수를 조회한다.
     *
     * @return int
     */
    int getQuizCount();

    /**
     * 특정 유형에서 USAGE_COUNT가 주어진 값 미만인 퀴즈 개수 반환
     *
     * @param questionType 문제 유형 ("뜻", "단어")
     * @param usageCount   기준값
     * @return 해당 조건에 맞는 퀴즈 개수
     */
    int getQuizCountByTypeAndUsageCount(@Param("questionType") String questionType,
        @Param("usageCount") int usageCount);

    /**
     * 하나의 퀴즈를 등록한다.
     *
     * @param quiz 생성할 퀴즈 객체
     */
    void insertQuiz(QuizMaster quiz);

    /**
     * usage_count가 지정된 값 이하인 퀴즈 중 랜덤하게 limit 개 조회
     *
     * @param usageCount 풀린횟수
     * @param limit      기준값
     * @return 퀴즈
     */
    List<QuizMaster> getQuizMastersForSpeed(@Param("usageCount") int usageCount,
        @Param("limit") int limit);

    /**
     * 특정 퀴즈 번호의 정답 번호를 조회한다.
     *
     * @param quizNo 퀴즈 번호
     * @return 정답 선택 번호
     */
    Integer getCorrectChoiceByQuizNo(int quizNo);

    /**
     * 여러 퀴즈 번호에 해당하는 퀴즈 정보 조회
     * @param quizNos 퀴즈 번호 리스트
     * @return 퀴즈 정보 리스트
     */
    List<QuizMaster> getQuizMastersByQuizNos(List<Integer> quizNos);

    /**
     * 난이도 및 유형에 따라 퀴즈 목록을 조회한다.
     *
     * @param difficulty   난이도 ('상', '중', '하')
     * @param questionType 문제 유형 ('뜻', '단어')
     * @return 퀴즈 목록
     */
    List<QuizMaster> getQuizListByDifficultyAndType(
        @Param("difficulty") String difficulty,
        @Param("questionType") String questionType
    );

    /**
     * 해당 퀴즈의 usage_count를 반환한다.
     * @param quizNo 퀴즈 번호
     * @return usage_count 값
     */
    int getUsageCount(int quizNo);

    /**
     * 세션 번호로 DIFFICULTY와 QUESTION_TYPE을 조회한다.
     * @param sessionNo 세션 번호
     * @return QuizLevelResultDto
     */
    QuizLevelResultDto getDifficultyAndQuestionTypeBySessionNo(@Param("sessionNo") int sessionNo);

}

