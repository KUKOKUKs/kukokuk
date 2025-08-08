package com.kukokuk.rest;

import com.kukokuk.service.QuizBookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz/bookmark")
public class ApiQuizBookmarkController {

    private final QuizBookmarkService quizBookmarkService;

    @PostMapping("/add")
    public Map<String, Object> addBookmark(@RequestBody Map<String, Object> req, Principal principal) {
        int quizNo = Integer.parseInt(req.get("quizNo").toString());
        int userNo = getUserNoFromPrincipal(principal);
        boolean success = quizBookmarkService.addBookmark(userNo, quizNo);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", success);
        return resp;
    }

    @PostMapping("/remove")
    public Map<String, Object> removeBookmark(@RequestBody Map<String, Object> req, Principal principal) {
        int quizNo = Integer.parseInt(req.get("quizNo").toString());
        int userNo = getUserNoFromPrincipal(principal);
        boolean success = quizBookmarkService.removeBookmark(userNo, quizNo);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", success);
        return resp;
    }

    private int getUserNoFromPrincipal(Principal principal) {
        // principal에서 userNo 추출 구현 필요
        return 1; // 임시
    }
}
