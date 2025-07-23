package com.kukokuk.dto;

import com.kukokuk.validation.EmailCheck;
import com.kukokuk.validation.PasswordCheck;
import com.kukokuk.validation.ProfileCheck;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class UserRegisterForm {

    @NotBlank(message = "이메일을 입력해 주세요", groups = EmailCheck.class)
    @Email(message = "유효한 이메일 형식이 아닙니다", groups = EmailCheck.class)
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요", groups = PasswordCheck.class)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[@$!%*?&])[a-zA-Z\\d@$!%*?&]{8,16}$",
        message = "유효한 비밀번호 형식이 아닙니다",
        groups = PasswordCheck.class)
    private String password;

    @NotBlank(message = "비밀번호 확인란을 입력해 주세요", groups = PasswordCheck.class)
    private String passwordConfirm;

    @NotBlank(message = "이름을 입력해 주세요", groups = ProfileCheck.class)
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "생년월일을 입력해 주세요", groups = ProfileCheck.class)
    @Past(message = "유효한 생년월일을 입력해 주세요", groups = ProfileCheck.class)
    private Date birthDate;

    @NotBlank(message = "성별을 선택해 주세요", groups = ProfileCheck.class)
    private String gender;

    @NotBlank(message = "닉네임을 입력해 주세요")
    @Size(min = 6, message = "6자 이상 입력해 주세요")
    private String nickname;

}
