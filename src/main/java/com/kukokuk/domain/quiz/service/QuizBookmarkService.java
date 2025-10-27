package com.kukokuk.domain.quiz.service;

import com.kukokuk.common.dto.Page;
import com.kukokuk.common.dto.Pagination;
import com.kukokuk.domain.quiz.dto.BookmarkedQuizDto;
import com.kukokuk.domain.quiz.mapper.QuizBookmarkedMapper;
import com.kukokuk.domain.quiz.vo.QuizBookmarked;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class QuizBookmarkService {

    private final QuizBookmarkedMapper quizBookmarkedMapper;

    /**
     * 전달 받은 페이지, 조회 조건에 해당하는 북마크한 이력 및 문제 목록 조회
     * @param page 조회할 페이지
     * @param condition 조회할 데이터의 조건 값들이 담겨 있는 Map 객체
     * @return 페이지네이션, 북마크한 이력 및 문제 목록 정보
     */
    public Page<BookmarkedQuizDto> getBookmarkQuizzes(int page, int rows, int userNo) {
        log.info("QuizBookmarkService getBookmarkQuizzes() 서비스 실행 page: {}, rows: {}", page, rows);

        // 페이징 처리에 필요한 객체 생성
        Page<BookmarkedQuizDto> bookmarkPage = new Page<>();

        // 사용자의 북마크 총 개수 조회
        int totalRows = quizBookmarkedMapper.getTotalRows(userNo);

        if (totalRows == 0) {
            // 조회할 데이터 행의 수가 없다면 빈 리스트 반환
            // 불필요한 DB 쿼리 발생 방지
            bookmarkPage.setItems(Collections.emptyList());
            return bookmarkPage;
        }

        // 페이징 처리 조건
        Pagination pagination = new Pagination(page, totalRows, rows); // 페이지네이션 객체 생성
        Map<String, Object> condition = new HashMap<>();
        condition.put("offset", pagination.getOffset());
        condition.put("rows", pagination.getRows());
        condition.put("userNo", userNo);

        // 조건에 해당하는 그룹 조회 요청
        List<BookmarkedQuizDto> quizzes = quizBookmarkedMapper.getBookmarkQuizzes(condition);

        // 페이지네이션 데이터 목록 세팅
        condition.remove("userNo"); // 필요하지 않은 값 제거(페이지네이션 쿼리스트링에 포함되지 않도록)
        bookmarkPage.setCondition(condition);
        bookmarkPage.setItems(quizzes);
        bookmarkPage.setPagination(pagination);

        return bookmarkPage;
    }

    /**
     * 퀴즈 북마크 등록
     *
     * @param userNo 사용자 번호
     * @param quizNo 퀴즈 번호
     * @return 성공 여부
     */
    public void insertBookmark(int userNo, int quizNo) {
        QuizBookmarked bookmark = new QuizBookmarked();
        bookmark.setUserNo(userNo);
        bookmark.setQuizNo(quizNo);
        quizBookmarkedMapper.insertQuizBookmarked(bookmark);
    }

    /**
     * 퀴즈 북마크 삭제
     *
     * @param userNo 사용자 번호
     * @param quizNo 퀴즈 번호
     * @return 성공 여부
     */
    public void deleteBookmark(int userNo, int quizNo) {
        quizBookmarkedMapper.deleteQuizBookmarkedByUserNoAndQuizNo(userNo, quizNo);
    }

}
