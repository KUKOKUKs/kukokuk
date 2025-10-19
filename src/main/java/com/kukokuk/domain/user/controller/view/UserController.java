package com.kukokuk.domain.user.controller.view;

import com.kukokuk.common.exception.AppException;
import com.kukokuk.common.util.FileValidationUtils;
import com.kukokuk.common.util.RequestPathUtils;
import com.kukokuk.domain.user.dto.UserFormDto;
import com.kukokuk.domain.user.service.UserService;
import com.kukokuk.domain.user.validation.UserModifyCheck;
import com.kukokuk.domain.user.vo.User;
import com.kukokuk.security.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
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

    private final ModelMapper modelMapper;
    private final UserService userService;

    // 세션에 바인딩할 폼 객체 초기화
    @ModelAttribute("userUpdateForm")
    public UserFormDto form(@AuthenticationPrincipal SecurityUser securityUser) {
        log.info("userUpdateForm 객체 초기화(로그인 사용자)");
        return modelMapper.map(securityUser.getUser(), UserFormDto.class);
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

            // 파일 저장 및 DB 업데이트 요청
            userService.updateUserProfileImage(file, securityUser.getUser().getUserNo());
            redirectAttributes.addFlashAttribute(
                "success", "프로필 이미지가 수정되었습니다."
            );
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
    public String profileModify(
        @Validated(UserModifyCheck.class) @ModelAttribute("userUpdateForm") UserFormDto form
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

        // 사용자 닉네임과 폼에 입력된 닉네임이 다를 경우(닉네임 변경 요청으로 판단)
        if (!form.getNickname().equals(securityUser.getUser().getNickname())) {
            log.info("profileModify() 닉네임 변경으로 중복검사 실행");

            // 폼에 입력된 닉네임으로 중복 확인
            boolean isDuplicatedNickname = userService.isDuplicatedByNickname(form.getNickname());
            log.info("profileModify() isDuplicatedByNickname: {}", isDuplicatedNickname);

            if (isDuplicatedNickname) {
                errors.rejectValue("nickname", "duplicated", "이미 사용중인 닉네임입니다.");
                return "user/profile/form";
            }
        }

        // 프로필 정보 수정 요청
        User updateUser = modelMapper.map(form, User.class);
        updateUser.setUserNo(securityUser.getUser().getUserNo());
        userService.updateUser(updateUser);

        redirectAttributes.addFlashAttribute(
            "success", "프로필 정보가 수정되었습니다."
        );

        return "redirect:/user/profile";
    }

    // 사용자 학습 진도/단계 수정 요청
    @PostMapping("/study-level")
    public String studyLevel(@ModelAttribute("userForm") UserFormDto form
        , @AuthenticationPrincipal SecurityUser securityUser
        , HttpServletRequest request) {
        log.info("studyLevel() 컨트롤러 실행");

        // 클라이언트에서 요청 보낸 페이지
        String pathAndQuery = RequestPathUtils.getRefererPathWithQuery(request);
        log.info("studyLevel() 요청 경로: {}", pathAndQuery);

        // 사용자 정보 업데이트 요청
        User updateUser = modelMapper.map(form, User.class);
        updateUser.setUserNo(securityUser.getUser().getUserNo());
        userService.updateUser(updateUser);

        // 클라이언트에서 요청 보낸 페이지로 리다이렉트
        return "redirect:" + pathAndQuery;
    }

}
