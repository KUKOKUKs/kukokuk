package com.kukokuk.domain.user.dto;

import com.kukokuk.domain.user.validation.EmailCheck;
import com.kukokuk.domain.user.validation.NicknameCheck;
import com.kukokuk.domain.user.validation.PasswordCheck;
import com.kukokuk.domain.user.validation.ProfileCheck;
import com.kukokuk.domain.user.validation.UserModifyCheck;
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
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserFormDto {

    @NotBlank(message = "이메일을 입력해 주세요", groups = EmailCheck.class)
    @Email(message = "유효한 이메일 형식이 아닙니다", groups = EmailCheck.class)
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요", groups = PasswordCheck.class)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[@$!%*?&])[a-zA-Z\\d@$!%*?&]{8,16}$"
        , message = "유효한 비밀번호 형식이 아닙니다"
        , groups = PasswordCheck.class)
    private String password;

    @NotBlank(message = "비밀번호 확인란을 입력해 주세요", groups = PasswordCheck.class)
    private String passwordConfirm;

    @NotBlank(message = "이름을 입력해 주세요", groups = {ProfileCheck.class, UserModifyCheck.class})
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "생년월일을 입력해 주세요", groups = {ProfileCheck.class, UserModifyCheck.class})
    @Past(message = "유효한 생년월일을 입력해 주세요", groups = {ProfileCheck.class, UserModifyCheck.class})
    private Date birthDate;

    @NotBlank(message = "성별을 선택해 주세요", groups = {ProfileCheck.class, UserModifyCheck.class})
    private String gender;

    @NotBlank(message = "닉네임을 입력해 주세요", groups = {NicknameCheck.class, UserModifyCheck.class})
    @Size(min = 4, max = 16, message = "4~16자로 입력해 주세요", groups = {NicknameCheck.class, UserModifyCheck.class})
    @Pattern(regexp = "^[a-zA-Z0-9가-힣_]{4,16}$"
        , message = "유효한 닉네임 형식이 아닙니다."
        , groups = {NicknameCheck.class, UserModifyCheck.class})
    private String nickname;

    private String profileFilename;
    private Integer level;
    private Integer experiencePoints;
    private String currentSchool; // ENUM("초등","중등")
    private Integer currentGrade;
    private Integer studyDifficulty;
    private Integer hintCount;
    private String isDeleted; // ENUM("N", "Y")
    
    private MultipartFile profileFile; // 프로필 이미지 파일

}
