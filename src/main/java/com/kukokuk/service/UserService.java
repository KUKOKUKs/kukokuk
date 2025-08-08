package com.kukokuk.service;

import com.kukokuk.dto.UserForm;
import com.kukokuk.exception.AppException;
import com.kukokuk.exception.UserFormException;
import com.kukokuk.mapper.UserMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

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
                userMapper.updateUserProfileImage(null, userNo);

                // 업데이트된 사용자 정보 조회하여 시큐리티 사용자 정보 갱신
                User updatedUser = getUserByUserNoWithRoleNames(userNo);
                SecurityUtil.updateAuthentication(updatedUser);
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
            userMapper.updateUserProfileImage(profileFilename, userNo);

            // 업데이트된 사용자 정보 조회하여 시큐리티 사용자 정보 갱신
            User updatedUser = getUserByUserNoWithRoleNames(userNo);
            SecurityUtil.updateAuthentication(updatedUser);
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
     * @param form   사용자 정보가 담긴 폼
     * @param userNo 사용자 번호
     */
    public void updateUser(UserForm form, int userNo) {
        log.info("updateUser() 서비스 실행");

        // 닉네임을 포함한 폼을 전달 받았을 경우
        if (form.getNickname() != null) {
            log.info("폼으로 전달 받은 닉네임: {}", form.getNickname());
            // 사용자 번호로 사용자 정보 조회
            User foundUserByUserNo = getUserByUserNo(userNo);

            // 사용자 닉네임과 폼에 입력된 닉네임이 다를 경우(닉네임 변경 요청으로 판단)
            if (!form.getNickname().equals(foundUserByUserNo.getNickname())) {
                log.info("닉네임 변경으로 중복검사 실행");

                // 폼에 입력된 닉네임으로 사용자 정보 조회
                User foundUserByNickname = getUserByNickname(form.getNickname());
                if (foundUserByNickname != null) {
                    throw new UserFormException("nickname", "이미 사용중인 닉네임입니다.");
                }
            }
        }

        User user = modelMapper.map(form, User.class);
        user.setUserNo(userNo);
        userMapper.updateUser(user);

        // 업데이트된 사용자 정보 조회하여 시큐리티 사용자 정보 갱신
        User updatedUser = getUserByUserNoWithRoleNames(userNo);
        SecurityUtil.updateAuthentication(updatedUser);
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
     * username을 전달받아 사용자 정보 조회
     * @param username username
     * @return 사용자 정보
     */
    public User getUserByUsername(String username) {
        log.info("getUserByUsername() 서비스 실행");
        return userMapper.getUserByUsername(username);
    }

    /**
     * nickname을 전달받아 사용자 정보 조회
     * @param nickname nickname
     * @return 사용자 정보
     */
    public User getUserByNickname(String nickname) {
        log.info("getUserByNickname() 서비스 실행");
        return userMapper.getUserByNickname(nickname);
    }

    /**
     * 회원가입 처리
     * @param form 신규 사용자 회원가입 정보
     */
    public void registerUser(UserForm form) {
        log.info("registerUser() 서비스 실행");
        // 폼 입력하는 동안 다른 사용자의 가입이 있을 경우를 대비하여
        // 중복 재확인(username, nickname)
        User foundUserByUsername = userMapper.getUserByUsername(form.getUsername());
        if (foundUserByUsername != null) {
            throw new UserFormException("username", "이미 사용중인 이메일입니다.");
        }
        User foundUserByNickname = userMapper.getUserByNickname(form.getNickname());
        if (foundUserByNickname != null) {
            throw new UserFormException("nickname", "이미 사용중인 닉네임입니다.");
        }

        // 폼 입력 값으로 User 객체 생성
        User user = modelMapper.map(form, User.class);
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(form.getPassword()));

        // 사용자, 사용자 권한 등록 처리
        userMapper.insertUser(user);
        userMapper.insertUserRole(user.getUserNo(), "ROLE_USER");
    }

    /**
     * 회원가입 이메일 중복 확인
     * @param username 이메일
     */
    public void duplicateUserByUsername(String username) {
        log.info("duplicateUserByUsername() 서비스 실행");
        User foundUser = userMapper.getUserByUsername(username);
        if (foundUser != null) {
            throw new UserFormException("username", "이미 사용중인 이메일입니다.");
        }
    }

    /**
     * 회원가입 닉네임 중복 확인
     * @param nickname 닉네임
     */
    public void duplicateUserByNickname(String nickname) {
        log.info("duplicateUserByNickname() 서비스 실행");
        User foundUser = userMapper.getUserByNickname(nickname);
        if (foundUser != null) {
            throw new UserFormException("nickname", "이미 사용중인 닉네임입니다.");
        }
    }

}
