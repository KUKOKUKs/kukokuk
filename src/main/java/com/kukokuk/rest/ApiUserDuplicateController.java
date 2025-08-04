package com.kukokuk.rest;

import com.kukokuk.response.ApiResponse;
import com.kukokuk.response.ResponseEntityUtils;
import com.kukokuk.service.UserService;
import com.kukokuk.vo.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class ApiUserDuplicateController {

    private final UserService userService;

    // username 중복 체크 요청
    @GetMapping("/duplicate/username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameDuplicate(@RequestParam("username") String username) {
        log.info("checkUsernameDuplicate() 컨트롤러 실행");
        User foundUser = userService.getUserByUsername(username);
        boolean isDuplicated = foundUser != null;
        return ResponseEntityUtils.ok(isDuplicated);
    }

    // nickname 중복 체크 요청
    @GetMapping("/duplicate/nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
        log.info("checkNicknameDuplicate() 컨트롤러 실행");
        User foundUser = userService.getUserByNickname(nickname);
        boolean isDuplicated = foundUser != null;
        return ResponseEntityUtils.ok(isDuplicated);
    }

}
