package com.kukokuk.controller;

import com.kukokuk.dto.UserUpdateForm;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.security.SecurityUtil;
import com.kukokuk.service.UserService;
import com.kukokuk.vo.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@RequiredArgsConstructor  // final 필드 기반 생성자 자동 생성
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    
    // 프로필 수정 폼
    @GetMapping("/profile")
    public String profileForm() {
        log.info("profileForm() 컨트롤러 실행");
        return "user/profile/form";
    }
    
    // 프로필 수정 요청
    @PostMapping("/profile")
    public String profileModify() {

        return "redirect:/user/profile";
    }

    // 사용자 학습 진도/단계 수정 요청
    @PostMapping("/study-level")
    public String studyLevel(@ModelAttribute UserUpdateForm form
        , @AuthenticationPrincipal SecurityUser securityUser
        , HttpServletRequest request) {
        log.info("studyLevel() 컨트롤러 실행");

        // 사용자 정보 업데이트 요청
        userService.updateUser(form, securityUser.getUser().getUserNo());

        // 업데이트된 사용자 정보 조회하여 시큐리티 사용자 정보 갱신
        User updatedUser = userService.getUserByUsernameWithRoleNames(securityUser.getUser().getUsername());
        SecurityUtil.updateAuthentication(updatedUser);

        // 클라이언트에서 요청 보낸 페이지로 리다이렉트
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

}
