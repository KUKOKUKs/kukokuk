package com.kukokuk.domain.quiz.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.domain.quiz.service.QuizBookmarkService;
import com.kukokuk.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [REST API] 퀴즈 북마크 추가/삭제 컨트롤러
 * - RESTful 방식 (POST: 추가, DELETE: 해제)
 * - 로그인 유저 정보에서 userNo 추출
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz/bookmark")
public class ApiQuizBookmarkController {

    private final QuizBookmarkService quizBookmarkService;

    @PostMapping("/{quizNo}")
    public ApiResponse<Void> addBookmark(@PathVariable int quizNo,
        @AuthenticationPrincipal SecurityUser securityUser) {
        quizBookmarkService.insertBookmark(securityUser.getUser().getUserNo(), quizNo);
        return ApiResponse.success("북마크 성공");
    }


    @DeleteMapping("/{quizNo}")
    public ApiResponse<Void> removeBookmark(@PathVariable int quizNo,
        @AuthenticationPrincipal SecurityUser securityUser) {
        quizBookmarkService.deleteBookmark(securityUser.getUser().getUserNo(), quizNo);
        return ApiResponse.success("북마크 해제 성공");
    }
}
