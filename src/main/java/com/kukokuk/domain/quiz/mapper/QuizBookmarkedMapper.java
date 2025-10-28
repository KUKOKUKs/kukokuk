package com.kukokuk.domain.quiz.mapper;

import com.kukokuk.domain.quiz.dto.BookmarkedQuizDto;
import com.kukokuk.domain.quiz.vo.QuizBookmarked;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 퀴즈 북마크 Mapper
 */
@Mapper
public interface QuizBookmarkedMapper {

    /**
     * 조건에 맞는 북마크한 이력 및 문제 목록 정보 조회
     * @param condition 조회할 데이터의 조건 값들이 담겨 있는 Map 객체 offset, rows, userNo
     * @return 북마크한 이력 및 문제 목록 정보
     */
    List<BookmarkedQuizDto> getBookmarkQuizzes(Map<String, Object> condition);

    /**
     * 조회할 데이터의 총 행의 수 조회
     * @param condition 조회할 데이터의 조건 값들이 담겨 있는 Map 객체
     * @return 조회할 데이터의 총 데이터 행의 수
     */
    int getTotalRows(int userNo);

    /**
     * 퀴즈 북마크 등록
     *
     * @param quizBookmarked 북마크 정보
     */
    void insertQuizBookmarked(QuizBookmarked quizBookmarked);

    /**
     * 퀴즈 북마크 삭제
     *
     * @param userNo 사용자 번호
     * @param quizNo 퀴즈 번호
     */
    void deleteQuizBookmarkedByUserNoAndQuizNo(@Param("userNo") int userNo,
        @Param("quizNo") int quizNo);

}
