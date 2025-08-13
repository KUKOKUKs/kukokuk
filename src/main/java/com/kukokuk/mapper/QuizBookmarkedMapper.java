package com.kukokuk.mapper;

import com.kukokuk.dto.BookmarkedQuizDto;
import com.kukokuk.vo.QuizBookmarked;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 퀴즈 북마크 Mapper
 */
@Mapper
public interface QuizBookmarkedMapper {

    /**
     * 특정 사용자와 퀴즈 번호에 대해 북마크 여부를 조회한다.
     *
     * @param userNo 사용자 번호
     * @param quizNo 퀴즈 번호
     * @return 북마크 정보 (존재하지 않으면 null)
     */
    QuizBookmarked getQuizBookmarkedByUserNoAndQuizNo(@Param("userNo") int userNo,
        @Param("quizNo") int quizNo);

    /**
     * 퀴즈 북마크 등록
     *
     * @param quizBookmarked 북마크 정보
     * @return 등록된 row 수
     */
    int insertQuizBookmarked(QuizBookmarked quizBookmarked);

    /**
     * 퀴즈 북마크 삭제
     *
     * @param userNo 사용자 번호
     * @param quizNo 퀴즈 번호
     * @return 삭제된 row 수
     */
    int deleteQuizBookmarkedByUserNoAndQuizNo(@Param("userNo") int userNo,
        @Param("quizNo") int quizNo);

    /**
     * 해당 사용자의 모든 북마크 목록 조회
     *
     * @param userNo 사용자 번호
     * @return 북마크 리스트
     */
    List<QuizBookmarked> getQuizBookmarkedListByUserNo(int userNo);

    /**
     * 해당 사용자의 북마크 퀴즈 목록 조회 (페이징)
     * @param userNo 사용자 번호
     * @param offset 시작 위치
     * @param limit 조회 개수
     * @return List<BookmarkedQuizDto>
     */
    List<BookmarkedQuizDto> getBookmarkedQuizzes(
        @Param("userNo") int userNo,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    /**
     * 해당 사용자의 북마크 퀴즈 총 개수 조회
     * @param userNo 사용자 번호
     * @return int (북마크된 퀴즈 개수)
     */
    int getCountBookmarkedQuizzes(int userNo);
}
