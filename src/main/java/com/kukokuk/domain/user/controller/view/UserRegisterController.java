package com.kukokuk.domain.user.controller.view;

import com.kukokuk.domain.user.dto.UserFormDto;
import com.kukokuk.domain.user.service.UserService;
import com.kukokuk.domain.user.validation.EmailCheck;
import com.kukokuk.domain.user.validation.NicknameCheck;
import com.kukokuk.domain.user.validation.PasswordCheck;
import com.kukokuk.domain.user.validation.ProfileCheck;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
@SessionAttributes("userRegisterForm") // 세션 유지할 모델 이름
@RequiredArgsConstructor  // final 필드 기반 생성자 자동 생성
@RequestMapping("/register")
public class UserRegisterController {

    private final UserService userService;

    // 세션에 바인딩할 폼 객체 초기화
    @ModelAttribute("userRegisterForm")
    public UserFormDto form() {
        log.info("UserRegisterForm 객체 초기화");
        return new UserFormDto();
    }

    // 1단계: 이메일 입력 폼
    @GetMapping
    public String emailForm(@RequestParam(required = false) Boolean reset
        , SessionStatus sessionStatus) {
        log.info("emailForm() 컨트롤러 실행");

        if (Boolean.TRUE.equals(reset)) {
            log.info("emailForm() reset=true으로 세션 초기화");
            sessionStatus.setComplete(); // 세션 초기화
            return "redirect:/register";
        }

        return "user/register/email";
    }

    // 1단계: 이메일 처리 후 → 비밀번호 단계로 이동
    @PostMapping("/email")
    public String processEmail(@Validated(EmailCheck.class) @ModelAttribute("userRegisterForm") UserFormDto form
        , BindingResult errors
        , Model model) {
        log.info("processEmail() 컨트롤러 실행");

        // 유효성 검증 실패 시 다시 입력 페이지
        if (errors.hasErrors()) {
            log.info("username 필드 오류: {}", Objects.requireNonNull(
                errors.getFieldError("username")).getDefaultMessage());
            model.addAttribute("hasError", true); // 에러 플래그 전달
            return "user/register/email";
        }

        // username 중복 체크
        boolean isDuplicatedUsername = userService.isDuplicatedByUsername(form.getUsername());
        log.info("processEmail() isDuplicatedUsername: {}", isDuplicatedUsername);

        if (isDuplicatedUsername) {
            errors.rejectValue("username", "duplicated", "이미 사용중인 이메일입니다.");
            model.addAttribute("hasError", true); // 에러 플래그 전달
            return "user/register/email";
        }

        return "redirect:/register/password"; // 성공 시 다음 단계로 리다이렉트
    }

    // 2단계: 비밀번호 입력 폼
    @GetMapping("/password")
    public String passwordForm() {
        log.info("passwordForm() 컨트롤러 실행");
        return "user/register/password";
    }

    // 2단계: 비밀번호 처리 후 → 이름/생년월일/성별 단계로 이동
    @PostMapping("/password")
    public String processPassword(@Validated(PasswordCheck.class) @ModelAttribute("userRegisterForm") UserFormDto form
        , BindingResult errors
        , Model model) {
        log.info("processPassword() 컨트롤러 실행");

        if (errors.hasErrors()) {
            if (errors.hasFieldErrors("password")) {
                log.info("password 필드 오류: {}", Objects.requireNonNull(
                    errors.getFieldError("password")).getDefaultMessage());
            }

            if (errors.hasFieldErrors("passwordConfirm")) {
                log.info("passwordConfirm 필드 오류: {}", Objects.requireNonNull(
                    errors.getFieldError("passwordConfirm")).getDefaultMessage());
            }

            model.addAttribute("hasError", true); // 에러 플래그 전달
            return "user/register/password"; // 유효성 검증 실패 시 다시 입력 페이지
        }

        // 비밀번호 일치 확인
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            errors.rejectValue("passwordConfirm", "Match", "비밀번호가 일치하지 않습니다");
            model.addAttribute("hasError", true); // 에러 플래그 전달
            return "user/register/password"; // 유효성 검증 실패 시 다시 입력 페이지
        }

        return "redirect:/register/profile"; // 성공 시 다음 단계로 리다이렉트
    }

    // 3단계: 이름/생년월일/성별 입력 폼
    @GetMapping("/profile")
    public String profileForm(@ModelAttribute("userRegisterForm") UserFormDto form) {
        log.info("profileForm() 컨트롤러 실행");
        form.setGender("M"); // 성별 기본값 설정
        return "user/register/profile";
    }

    // 3단계: 이름/생년월일/성별 처리 후 → 닉네임 단계로 이동
    @PostMapping("/profile")
    public String processProfile(@Validated(ProfileCheck.class) @ModelAttribute("userRegisterForm") UserFormDto form
        , BindingResult errors
        , Model model) {
        log.info("processProfile() 컨트롤러 실행");

        if (errors.hasErrors()) {
            if (errors.hasFieldErrors("name")) {
                log.info("name 필드 오류: {}", Objects.requireNonNull(
                    errors.getFieldError("name")).getDefaultMessage());
            }

            if (errors.hasFieldErrors("birthDate")) {
                log.info("birthDate 필드 오류: {}", Objects.requireNonNull(
                    errors.getFieldError("birthDate")).getDefaultMessage());
            }

            model.addAttribute("hasError", true); // 에러 플래그 전달
            return "user/register/profile"; // 유효성 검증 실패 시 다시 입력 페이지
        }

        return "redirect:/register/nickname"; // 성공 시 다음 단계로 리다이렉트
    }

    // 4단계: 닉네임 입력 폼
    @GetMapping("/nickname")
    public String nicknameForm() {
        log.info("nicknameForm() 컨트롤러 실행");
        return "user/register/nickname";
    }

    // 4단계: 닉네임 처리 후 → 회원가입 처리 후 -> 회원가입 완료 페이지 이동
    @PostMapping("/nickname")
    public String processNickname(@Validated(NicknameCheck.class) @ModelAttribute("userRegisterForm") UserFormDto form
        , BindingResult errors
        , Model model) {
        log.info("processNickname() 컨트롤러 실행");

        // 유효성 검증 실패 시 다시 입력 페이지
        if (errors.hasErrors()) {
            log.info("nickname() 필드 오류: {}", Objects.requireNonNull(
                errors.getFieldError("nickname")).getDefaultMessage());
            model.addAttribute("hasError", true); // 에러 플래그 전달
            return "user/register/nickname";
        }

        // nickname 중복 체크
        boolean isDuplicatedNickname = userService.isDuplicatedByNickname(form.getNickname());
        log.info("processNickname() isDuplicatedByNickname: {}", isDuplicatedNickname);

        if (isDuplicatedNickname) {
            errors.rejectValue("nickname", "duplicated", "이미 사용중인 닉네임입니다.");
            model.addAttribute("hasError", true); // 에러 플래그 전달
            return "user/register/nickname";
        }

        // 폼 입력하는 동안 다른 사용자의 가입이 있을 경우를 대비하여
        // 중복 재확인(username)
        boolean isDuplicatedUsername = userService.isDuplicatedByUsername(form.getUsername());
        log.info("processNickname() isDuplicatedUsername: {}", isDuplicatedUsername);

        if (isDuplicatedUsername) {
            errors.rejectValue("username", "duplicated", "이미 사용중인 이메일입니다.");
            model.addAttribute("hasError", true); // 에러 플래그 전달
            return "user/register/email";
        }
        
        userService.registerUser(form); // 사용자, 권한 등록 요청

        return "redirect:/register/complete"; // 성공 시 회원가입 성공 페이지로 리다이렉트
    }

    // 회원가입 완료
    @GetMapping("/complete")
    public String registerComplete() {
        log.info("registerComplete() 컨트롤러 실행");
        return "user/register/complete";
    }

}
