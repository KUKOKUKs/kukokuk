package com.kukokuk.service;

import com.kukokuk.dto.UserRegisterForm;
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
     * 회원가입 처리
     * @param form 신규 사용자 회원가입 정보
     */
    public void registerUser(UserRegisterForm form) {
        log.info("registerUser() 실행");
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
        log.info("registerUserByUsername() 실행");
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
        log.info("registerUserByNickname() 실행");
        User foundUser = userMapper.getUserByNickname(nickname);
        if (foundUser != null) {
            throw new UserRegisterException("nickname", "이미 사용중인 닉네임입니다.");
        }
    }

}
