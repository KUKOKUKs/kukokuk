package com.kukokuk.controller;

import com.kukokuk.dto.UserForm;
import com.kukokuk.exception.AppException;
import com.kukokuk.exception.UserFormException;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.UserService;
import com.kukokuk.util.FileValidationUtils;
import com.kukokuk.validation.UserModifyCheck;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Log4j2
@Controller
@SessionAttributes("userUpdateForm") // 세션 유지할 모델 이름
@RequiredArgsConstructor  // final 필드 기반 생성자 자동 생성
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    // 세션에 바인딩할 폼 객체 초기화
    @ModelAttribute("userUpdateForm")
    public UserForm form(@AuthenticationPrincipal SecurityUser securityUser) {
        log.info("userUpdateForm 객체 초기화(로그인 사용자)");
        return modelMapper.map(securityUser.getUser(), UserForm.class);
    }

    // 프로필 이미지 삭제 요청
    @PostMapping("/profile-img-delete")
    public String deleteProfileImage(@AuthenticationPrincipal SecurityUser securityUser
        , RedirectAttributes redirectAttributes) {
        log.info("deleteProfileImage() 컨트롤러 실행");

        try {
            userService.deleteUserProfileImage(securityUser.getUser().getProfileFilename()
                , securityUser.getUser().getUserNo());
        } catch (IllegalArgumentException | IllegalStateException | AppException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/user/profile";
    }
    
    // 프로필 이미지 등록 요청
    @PostMapping("/profile-img-update")
    public String uploadProfileImage(@RequestParam("profileFile") MultipartFile file
        , @AuthenticationPrincipal SecurityUser securityUser
        , RedirectAttributes redirectAttributes) {
        log.info("uploadProfile() 컨트롤러 실행");

        try {
            FileValidationUtils.validateProfileImage(file); // 파일 유효성 검사
            userService.updateUserProfileImage(file, securityUser.getUser().getUserNo()); // 파일 저장 및 DB 업데이트 요청
            redirectAttributes.addFlashAttribute("success", "프로필 이미지가 수정되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException | AppException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/user/profile";
    }

    // 프로필 수정 폼
    @GetMapping("/profile")
    public String profileForm(@RequestParam(required = false) Boolean reset
        , SessionStatus sessionStatus) {
        log.info("profileForm() 컨트롤러 실행");

        if (Boolean.TRUE.equals(reset)) {
            log.info("profileForm() reset=true으로 세션 초기화");
            sessionStatus.setComplete(); // 세션 초기화
            return "redirect:/user/profile";
        }

        return "user/profile/form";
    }

    // 프로필 수정 요청
    @PostMapping("/profile")
    public String profileModify(@Validated(UserModifyCheck.class) @ModelAttribute("userUpdateForm") UserForm form
        , @AuthenticationPrincipal SecurityUser securityUser
        , BindingResult errors
        , RedirectAttributes redirectAttributes) {
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

        redirectAttributes.addFlashAttribute("success", "프로필 정보가 수정되었습니다.");
        return "redirect:/user/profile";
    }

    // 사용자 학습 진도/단계 수정 요청
    @PostMapping("/study-level")
    public String studyLevel(@ModelAttribute("userForm") UserForm form
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
