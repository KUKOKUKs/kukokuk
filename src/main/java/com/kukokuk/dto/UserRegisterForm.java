package com.kukokuk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kukokuk.validation.EmailCheck;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterForm {

    @NotBlank(message = "이메일을 입력해 주세요", groups = EmailCheck.class)
    @Email(message = "유효한 이메일 형식이 아닙니다", groups = EmailCheck.class)
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요")
    @Size(min = 8, message = "8자 이상 입력해 주세요")
    private String password;

    @NotBlank(message = "이름을 입력해 주세요")
    private String name;

    @NotBlank(message = "닉네임을 입력해 주세요")
    @Size(min = 6, message = "6자 이상 입력해 주세요")
    private String nickname;

    @NotNull(message = "생년월일을 입력해 주세요")
    @Past(message = "유효한 생년월일을 입력해 주세요")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date birthDate;

    @NotBlank(message = "성별을 선택해 주세요")
    private String gender;

}
