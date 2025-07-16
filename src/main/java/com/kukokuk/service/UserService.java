package com.kukokuk.service;

import com.kukokuk.exception.UserRegisterException;
import com.kukokuk.mapper.UserMapper;
import com.kukokuk.vo.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    /**
     * 회원가입 이메일 중복 확인
     * @param username 이메일
     */
    public void registerUserByUsername(String username) {
        User foundUser = userMapper.getUserByUsername(username);
        if (foundUser != null) {
            throw new UserRegisterException("username", "이미 사용중인 이메일입니다.");
        }
    }

    /**
     * 회원가입 닉네임 중복 확인
     * @param nickname 닉네임
     */
    public void registerUserByNickname(String nickname) {
        User foundUser = userMapper.getUserByNickname(nickname);
        if (foundUser != null) {
            throw new UserRegisterException("nickname", "이미 사용중인 닉네임입니다.");
        }
    }

}
