package com.kukokuk.exception;

import java.io.Serial;
import lombok.Getter;

/*
 * 회원가입 예외를 표현하는 클래스다.
 * 	field는 유효성 검증을 통과하지 못한 입력필드 이름이다.
 *  message는 유효성 검증 실패 메세지다.
 */
@Getter
public class UserRegisterException extends AppException {

    @Serial
    private static final long serialVersionUID = -4860672033504039964L;

    private final String field;

    public UserRegisterException(String field, String message) {
        super(message);
        this.field = field;
    }

}
