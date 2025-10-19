package com.kukokuk.domain.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupFormDto {

    @NotBlank(message = "우리반 이름을 입력해 주세요")   // null, "", " " 방지
    @Size(max = 20, message = "20자 이내로 입력해주세요") // 최대 20자 제한
    private String title;

    @Pattern(regexp = "^$|^[0-9]{4}$", message = "숫자 4자리여야 합니다")
    private String password; // 선택 입력 (null 허용)

    @Size(max = 20, message = "20자 이내로 입력해주세요")
    private String motto;    // 선택 입력

    private Integer groupNo;        // 그룹 번호
    private boolean deletePassword; // 기존 비밀번호 제거(공개방으로 설정)

}
