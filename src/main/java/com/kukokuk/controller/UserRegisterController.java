package com.kukokuk.controller;

import com.kukokuk.dto.UserRegisterForm;
import com.kukokuk.exception.UserRegisterException;
import com.kukokuk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("userRegisterForm") // 세션 유지할 모델 이름
@RequiredArgsConstructor  // final 필드 기반 생성자 자동 생성 (Lombok)
@RequestMapping("/register")
public class UserRegisterController {

    private final UserService userService;

    // 세션에 바인딩할 폼 객체 초기화
    @ModelAttribute("userRegisterForm")
    public UserRegisterForm form() {
        return new UserRegisterForm();
    }

    // 1단계: 이메일 입력 폼
    @GetMapping
    public String emailForm() {
        return "user/register/email";
    }

    // 1단계: 이메일 처리 후 → 비밀번호 단계로 이동
    @PostMapping("/email")
    public String processEmail(@ModelAttribute("userRegisterForm") UserRegisterForm form
        , BindingResult errors) {
        // 유효성 검증 실패 시 다시 입력 페이지
        if (errors.hasErrors()) {
            return "user/register/email";
        }

        try {
            // username 중복 체크
            userService.registerUserByUsername(form.getUsername());
        } catch (UserRegisterException e) {
            errors.rejectValue(e.getField(), "duplicated", e.getMessage());
            return "user/register/email";
        }

        return "redirect:/register/password"; // 성공 시 다음 단계로 리다이렉트
    }

    // 2단계: 비밀번호 입력 폼
    @GetMapping("/password")
    public String passwordForm() {
        return "user/register/password";
    }

    // 2단계: 비밀번호 처리 후 → 이름 단계로 이동
    @PostMapping("/password")
    public String processPassword(@ModelAttribute("userRegisterForm") UserRegisterForm form
        , BindingResult errors) {
        if (errors.hasErrors()) {
            return "user/register/password"; // 유효성 검증 실패 시 다시 입력 페이지
        }

        return "redirect:/register/name"; // 성공 시 다음 단계로 리다이렉트
    }

    // 3단계: 이름 입력 폼
    @GetMapping("/name")
    public String nameForm() {
        return "user/register/name";
    }

    // 3단계: 이름 처리 후 → 닉네임 단계로 이동
    @PostMapping("/name")
    public String processName(@ModelAttribute("userRegisterForm") UserRegisterForm form
        , BindingResult errors) {
        if (errors.hasErrors()) {
            return "user/register/name"; // 유효성 검증 실패 시 다시 입력 페이지
        }

        return "redirect:/register/nickname"; // 성공 시 다음 단계로 리다이렉트
    }

    // 4단계: 닉네임 입력 폼
    @GetMapping("/nickname")
    public String nicknameForm() {
        return "user/register/nickname";
    }

    // 4단계: 닉네임 처리 후 → 생년월일/성별 단계로 이동
    @PostMapping("/nickname")
    public String processNickname(@ModelAttribute("userRegisterForm") UserRegisterForm form
        , BindingResult errors) {
        // 유효성 검증 실패 시 다시 입력 페이지
        if (errors.hasErrors()) {
            return "user/register/nickname";
        }

        try {
            // nickname 중복 체크
            userService.registerUserByNickname(form.getNickname());
        } catch (UserRegisterException e) {
            errors.rejectValue(e.getField(), "duplicated", e.getMessage());
            return "user/register/nickname";
        }

        return "redirect:/register/profile"; // 성공 시 다음 단계로 리다이렉트
    }

    // 5단계: 생년월일/성별 입력 폼
    @GetMapping("/profile")
    public String profileForm() {
        return "user/register/profile";
    }

    // 5단계: 생년월일/성별 처리 후 → 회원가입 요청
    @PostMapping("/profile")
    public String processProfile(@ModelAttribute("userRegisterForm") UserRegisterForm form
        , BindingResult errors) {
        if (errors.hasErrors()) {
            return "user/register/profile"; // 유효성 검증 실패 시 다시 입력 페이지
        }

        try {
            // username, nickname 중복 체크 후 사용자, 사용자 권한 등록 요청
            userService.registerUser(form);
        } catch (UserRegisterException e) {
            errors.rejectValue(e.getField(), "duplicated", e.getMessage());
            // 각 오류 필드로 이동
            if ("username".equals(e.getField())) {
                return "user/register/email";
            }
            if ("nickname".equals(e.getField())) {
                return "user/register/nickname";
            }
        }

        return "redirect:/register/complete"; // 성공 시 회원가입 성공 페이지로 리다이렉트
    }

    // 회원가입 완료
    @GetMapping("/complete")
    public String registerComplete() {
        return "user/register/complete";
    }

}
