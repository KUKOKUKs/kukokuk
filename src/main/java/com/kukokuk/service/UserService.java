package com.kukokuk.service;

import com.kukokuk.dto.UserForm;
import com.kukokuk.exception.AppException;
import com.kukokuk.mapper.UserMapper;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.security.SecurityUtil;
import com.kukokuk.util.FileValidationUtils;
import com.kukokuk.vo.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
@RequiredArgsConstructor // 초기화 되지않은 final 필드나, @NonNull 이 붙은 필드에 대해 생성자를 생성
public class UserService {

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * 사용자 힌트 개수 +1 업데이트
     * @param userNo 사용자 번호
     */
    public void updateUserHintCountPlus(int userNo) {
        log.info("updateUserHintCountPlus() 서비스 실행");
        userMapper.updateUserHintCountPlus(1, userNo);
    }

    /**
     * 전달받은 힌트 개수를 사용자 힌트 개수에 더하여 업데이트
     * @param hintCount 추가할 힌트 개수
     * @param userNo 사용자 번호
     */
    public void updateUserHintCountPlus(int hintCount, int userNo) {
        log.info("updateUserHintCountPlus({}) 서비스 실행", hintCount);
        userMapper.updateUserHintCountPlus(hintCount, userNo);

        // 현재 로그인 사용자의 Authentication 갱신
        updateAuthentication(userNo);
    }

    /**
     * 사용자 프로필 이미지 삭제 및 profileFilename 업데이트
     * @param profileFilename 프로필 이미지 파일명
     * @param userNo 사용자 번호
     */
    @Transactional
    public void deleteUserProfileImage(String profileFilename, int userNo) {
        log.info("deleteUserProfileImage() 서비스 실행");

        if (profileFilename != null) {
            // 이미지 파일이 저장된 디렉토리
            Path uploadDir = Paths.get("C:/kukokuk/user/profileImage/" + userNo + "/" + profileFilename);

            try {
                Files.deleteIfExists(uploadDir); // 파일 삭제
                userMapper.updateUserProfileImage(userNo, null);

                // 현재 로그인 사용자의 Authentication 갱신
                updateAuthentication(userNo);
            } catch (IOException ioException) {
                log.warn("파일 삭제 실패: {}", ioException.getMessage());
            }
        } else {
            throw new AppException("잘못된 요청입니다.");
        }
    }

    /**
     * 사용자 프로필 이미지 파일 저장 및 profileFilename 업데이트
     * @param file   이미지 파일
     * @param userNo 사용자 번호
     */
    @Transactional
    public void updateUserProfileImage(MultipartFile file, int userNo) {
        log.info("updateUserProfileImage() 서비스 실행");

        String profileFilename = null;
        Path destinationPath = null;

        // 파일이 존재하고 비어있지 않을 경우
        if (file != null && !file.isEmpty()) {
            // 저장 디렉토리
            Path uploadDir = Paths.get("C:/kukokuk/user/profileImage/" + userNo);

            try {
                // 디렉토리가 없다면 생성
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                // 클라이언트가 업로드한 원본 파일 이름, 확장자 추출(예: "profile.jpg", ".jpg")
                String originalFilename = file.getOriginalFilename();
                String extension = FileValidationUtils.extractExtension(originalFilename);

                // UUID를 기반으로 새로운 파일 이름 생성
                String savedFilename = UUID.randomUUID().toString().replace("-", "") + extension;
                profileFilename = savedFilename;
                log.info("profileFilename: {}", profileFilename);

                // 최종 저장 경로
                destinationPath = uploadDir.resolve(savedFilename);
                log.info("destinationPath: {}", destinationPath);

                // 파일 저장 (Path 사용)
                file.transferTo(destinationPath);

            } catch (IOException e) {
                throw new IllegalStateException("파일 저장 중 오류가 발생했습니다.", e);
            }
        }

        // DB 저장 시 오류 발생하면 파일 삭제
        try {
            userMapper.updateUserProfileImage(userNo, profileFilename);

            // 현재 로그인 사용자의 Authentication 갱신
            updateAuthentication(userNo);
        } catch (DataAccessException e) {
            if (destinationPath != null) {
                try {
                    Files.deleteIfExists(destinationPath);
                } catch (IOException ioException) {
                    log.warn("DB 오류 후 파일 삭제 실패: {}", ioException.getMessage());
                }
            }
            throw new AppException("데이터베이스 작업 중 오류가 발생했습니다.\n다시 시도해 주세요.");
        }
    }

    /**
     * 사용자 정보 업데이트
     * @param updateUser 업데이트할 사용자 정보
     */
    @Transactional
    public void updateUser(User updateUser) {
        log.info("updateUser() 서비스 실행");

        userMapper.updateUser(updateUser);

        // 현재 로그인 사용자의 Authentication 갱신
        updateAuthentication(updateUser.getUserNo());
    }

    /**
     * 현재 로그인 사용자의 Authentication 갱신
     * 사용자 정보 업데이트 후 사용됨
     * principal로 표현식을 사용하는 html에 적용되도록 함
     * @param userNo 사용자 번호
     */
    void updateAuthentication(int userNo) {
        log.info("updateAuthentication() 서비스 실행");
        User updatedUser = getUserByUserNoWithRoleNames(userNo);
        SecurityUtil.updateAuthentication(updatedUser);
    }

    /**
     * 스프링시큐리티에 캐싱된 사용자 정보 가져오기
     * 서비스단에서 사용할 목적
     * 컨트롤러단에서는 SecurityUser 활용
     * @return 사용자 정보
     */
    public User getCurrentUser() {
        log.info("getCurrentUser() 서비스 실행");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // 인증되지 않은 경우
        }

        Object principal = authentication.getPrincipal();

        // principal instanceof 체크 후 캐스팅하여
        // SecurityUser 내부의 getUser() 호출
        if (principal instanceof SecurityUser securityUser) {
            return securityUser.getUser();
        } else {
            return null; // 예상치 못한 경우
        }
    }

    /**
     * 사용자 번호로 사용자 정보, 권한 정보 조회
     * @param userNo 사용자 번호
     * @return 사용자 정보, 권한 정보
     */
    public User getUserByUserNoWithRoleNames(int userNo) {
        log.info("getUserByUserNoWithRoleNames() 서비스 실행");
        return userMapper.getUserByUserNoWithRoleNames(userNo);
    }

    /**
     * username으로 사용자 정보, 권한 정보 조회
     * @param username username
     * @return 사용자 정보, 권한 정보
     */
    public User getUserByUsernameWithRoleNames(String username) {
        log.info("getUserByUsernameWithRoleNames() 서비스 실행");
        return userMapper.getUserByUsernameWithRoleNames(username);
    }

    /**
     * 사용자 번호로 사용자 정보 조회
     * @param userNo 사용자 번호
     * @return 사용자 정보
     */
    public User getUserByUserNo(int userNo) {
        log.info("getUserByUserNo() 서비스 실행");
        return userMapper.getUserByUserNo(userNo);
    }

    /**
     * 회원가입 처리
     * @param form 신규 사용자 회원가입 정보
     */
    public void registerUser(UserForm form) {
        log.info("registerUser() 서비스 실행");

        // 폼 입력 값으로 User 객체 생성
        User user = modelMapper.map(form, User.class);
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(form.getPassword()));

        // 사용자, 사용자 권한 등록 처리
        userMapper.insertUser(user);
        userMapper.insertUserRole(user.getUserNo(), "ROLE_USER");
    }

    /**
     * 이메일 중복 여부
     * @param username 이메일
     * @return boolean true=중복 / false = 중복X
     */
    public boolean isDuplicatedByUsername(String username) {
        log.info("duplicateUserByUsername() 서비스 실행");
        return userMapper.isDuplicatedByUsername(username) > 0;
    }

    /**
     * 닉네임 중복 여부
     * @param nickname 닉네임
     * @return boolean true=중복 / false = 중복X
     */
    public boolean isDuplicatedByNickname(String nickname) {
        log.info("isDuplicatedByNickname() 서비스 실행");
        return userMapper.isDuplicatedByNickname(nickname) > 0;
    }

}
