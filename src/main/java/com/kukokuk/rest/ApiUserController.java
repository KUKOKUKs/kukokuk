package com.kukokuk.rest;

import com.kukokuk.response.ApiResponse;
import com.kukokuk.service.UserService;
import com.kukokuk.vo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class ApiUserController {

    private final UserService userService;

    public ResponseEntity<ApiResponse<User>> getUserByUsername(String username) {
        return null;
    }

}
