package com.kukokuk.service;

import com.kukokuk.mapper.QuizBookmarkedMapper;
import com.kukokuk.mapper.QuizMasterMapper;
import com.kukokuk.vo.QuizBookmarked;
import com.kukokuk.vo.QuizMaster;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizBookmarkService {
    private final QuizBookmarkedMapper quizBookmarkedMapper;
    private final QuizMasterMapper quizMasterMapper;

    /**
     * 퀴즈 북마크 등록
     * @param userNo 사용자 번호
     * @param quizNo 퀴즈 번호
     * @return 성공 여부
     */
    @Transactional
    public boolean addBookmark(int userNo, int quizNo) {
        // 이미 북마크 되어 있으면 중복 등록 방지
        QuizBookmarked existing = quizBookmarkedMapper.getQuizBookmarkedByUserNoAndQuizNo(userNo, quizNo);
        if (existing != null) return true;
        QuizBookmarked bookmark = new QuizBookmarked();
        bookmark.setUserNo(userNo);
        bookmark.setQuizNo(quizNo);
        int inserted = quizBookmarkedMapper.insertQuizBookmarked(bookmark);
        return inserted > 0;
    }

    /**
     * 퀴즈 북마크 삭제
     * @param userNo 사용자 번호
     * @param quizNo 퀴즈 번호
     * @return 성공 여부
     */
    @Transactional
    public boolean removeBookmark(int userNo, int quizNo) {
        int deleted = quizBookmarkedMapper.deleteQuizBookmarkedByUserNoAndQuizNo(userNo, quizNo);
        return deleted > 0;
    }

    /**
     * 북마크 여부 확인
     * @param userNo 사용자 번호
     * @param quizNo 퀴즈 번호
     * @return 북마크 여부
     */
    @Transactional(readOnly = true)
    public boolean isBookmarked(int userNo, int quizNo) {
        return quizBookmarkedMapper.getQuizBookmarkedByUserNoAndQuizNo(userNo, quizNo) != null;
    }

    /**
     * 해당 사용자의 모든 북마크 퀴즈 번호 목록 조회
     * @param userNo 사용자 번호
     * @return 북마크 리스트
     */
    @Transactional(readOnly = true)
    public List<QuizBookmarked> getBookmarkList(int userNo) {
        return quizBookmarkedMapper.getQuizBookmarkedListByUserNo(userNo);
    }

    /**
     * 해당 사용자의 북마크한 퀴즈 리스트(QuizMaster) 조회
     */
    public List<QuizMaster> getBookmarkedQuizList(int userNo) {
        List<QuizBookmarked> bookmarkList = quizBookmarkedMapper.getQuizBookmarkedListByUserNo(userNo);
        if (bookmarkList.isEmpty()) return Collections.emptyList();
        List<Integer> quizNos = bookmarkList.stream()
            .map(QuizBookmarked::getQuizNo)
            .collect(Collectors.toList());
        return quizMasterMapper.getQuizMastersByQuizNos(quizNos);
    }
}
