package com.kukokuk.service;

import com.kukokuk.dto.UserRegisterForm;
import com.kukokuk.dto.UserStudyLevelForm;
import com.kukokuk.exception.UserRegisterException;
import com.kukokuk.mapper.UserMapper;
import com.kukokuk.vo.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * 사용자 진도/단계 업데이트
     * @param form 사용자 진도, 단계 정보가 담긴 폼
     * @param userNo 사용자 번호
     */
    public void updateUserStudyLevel(UserStudyLevelForm form, int userNo) {
        log.info("updateUserStudyLevel() 서비스 실행");
        User user = modelMapper.map(form, User.class);
        user.setUserNo(userNo);
        userMapper.updateUser(user);
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
    public void registerUser(UserRegisterForm form) {
        log.info("registerUser() 서비스 실행");
        // 폼 입력하는 동안 다른 사용자의 가입이 있을 경우를 대비하여
        // 중복 재확인(username, nickname)
        User foundUserByUsername = userMapper.getUserByUsername(form.getUsername());
        if (foundUserByUsername != null) {
            throw new UserRegisterException("username", "이미 사용중인 이메일입니다.");
        }
        User foundUserByNickname = userMapper.getUserByNickname(form.getNickname());
        if (foundUserByNickname != null) {
            throw new UserRegisterException("nickname", "이미 사용중인 닉네임입니다.");
        }

        // 폼 입력 값으로 User 객체 생성
        User user = modelMapper.map(form, User.class);
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(form.getPassword()));

        // 사용자, 사용자 권한 등록 처리
        userMapper.insertUser(user);
        userMapper.insertUserRole(user.getUserNo(),"ROLE_USER");
    }

    /**
     * 회원가입 이메일 중복 확인
     * @param username 이메일
     */
    public void duplicateUserByUsername(String username) {
        log.info("duplicateUserByUsername() 서비스 실행");
        User foundUser = userMapper.getUserByUsername(username);
        if (foundUser != null) {
            throw new UserRegisterException("username", "이미 사용중인 이메일입니다.");
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
            throw new UserRegisterException("nickname", "이미 사용중인 닉네임입니다.");
        }
    }

}
