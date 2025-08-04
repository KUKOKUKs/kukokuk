package com.kukokuk.controller;

import com.kukokuk.dto.UserForm;
import com.kukokuk.exception.UserFormException;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.UserService;
import com.kukokuk.validation.UserModifyCheck;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Log4j2
@Controller
@SessionAttributes("userUpdateForm") // 세션 유지할 모델 이름
@RequiredArgsConstructor  // final 필드 기반 생성자 자동 생성
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // 세션에 바인딩할 폼 객체 초기화
    @ModelAttribute("userUpdateForm")
    public UserForm form() {
        log.info("userUpdateForm 객체 초기화");
        return new UserForm();
    }
    
    // 프로필 수정 폼
    @GetMapping("/profile")
    public String profileForm(@RequestParam(required = false) Boolean reset
        , SessionStatus sessionStatus) {
        log.info("profileForm() 컨트롤러 실행");

        if (Boolean.TRUE.equals(reset)) {
            log.info("emailForm() reset=true으로 세션 초기화");
            sessionStatus.setComplete(); // 세션 초기화
            return "redirect:/profile";
        }

        return "user/profile/form";
    }

    // 프로필 수정 요청
    @PostMapping("/profile")
    public String profileModify(@Validated(UserModifyCheck.class) @ModelAttribute("userUpdateForm") UserForm form
        , @AuthenticationPrincipal SecurityUser securityUser
        , BindingResult errors
        , Model model) {
        log.info("profileModify() 컨트롤러 실행");

        // 유효성 검증 실패 시 다시 입력 페이지
        if (errors.hasErrors()) {
            if (errors.hasFieldErrors("name")) {
                log.info("name 필드 오류: {}", Objects.requireNonNull(
                    errors.getFieldError("name")).getDefaultMessage());
            }

            if (errors.hasFieldErrors("birthDate")) {
                log.info("birthDate 필드 오류: {}", Objects.requireNonNull(
                    errors.getFieldError("birthDate")).getDefaultMessage());
            }

            if (errors.hasFieldErrors("nickname")) {
                log.info("nickname() 필드 오류: {}", Objects.requireNonNull(
                    errors.getFieldError("nickname")).getDefaultMessage());
            }

            if (errors.hasFieldErrors("gender")) {
                log.info("gender() 필드 오류: {}", Objects.requireNonNull(
                    errors.getFieldError("gender")).getDefaultMessage());
            }

            model.addAttribute("hasError", true); // 에러 플래그 전달
            return "user/profile/form"; // 유효성 검증 실패 시 다시 입력 페이지
        }

        // nickname 중복 체크 후 사용자 정보 업데이트 요청
        try {
            userService.updateUser(form, securityUser.getUser().getUserNo());
        } catch (UserFormException e) {
            log.info("profileModify() UserFormException {}", e.getMessage());
            errors.rejectValue(e.getField(), "duplicated", e.getMessage());
            return "user/profile/form";
        }

        return "redirect:/user/profile";
    }

    // 사용자 학습 진도/단계 수정 요청
    @PostMapping("/study-level")
    public String studyLevel(@ModelAttribute("userUpdateForm") UserForm form
        , @AuthenticationPrincipal SecurityUser securityUser
        , HttpServletRequest request) {
        log.info("studyLevel() 컨트롤러 실행");

        // 클라이언트에서 요청 보낸 페이지
        String referer = request.getHeader("Referer");
        URI uri = URI.create(referer);
        String path = uri.getPath();
        log.info("studyLevel() 요청 path: {}", path);

        // 사용자 정보 업데이트 요청
        userService.updateUser(form, securityUser.getUser().getUserNo());

        // 클라이언트에서 요청 보낸 페이지로 리다이렉트
        return "redirect:" + (path != null ? path : "/");
    }
}
