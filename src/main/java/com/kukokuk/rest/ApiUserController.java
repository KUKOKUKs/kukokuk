package com.kukokuk.rest;

import com.kukokuk.response.ApiResponse;
import com.kukokuk.response.ResponseEntityUtils;
import com.kukokuk.service.UserService;
import com.kukokuk.vo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class ApiUserController {

    private final UserService userService;

    // username 중복 체크 요청
    @GetMapping("/username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameDuplicate(@RequestParam("username") String username) {
        User foundUser = userService.getUserByUsername(username);
        boolean isDuplicated = foundUser != null;
        return ResponseEntityUtils.ok(isDuplicated);
    }

}
