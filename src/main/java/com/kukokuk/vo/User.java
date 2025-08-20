package com.kukokuk.vo;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("User")
public class User {

    private int userNo;
    private String username; // 이메일
    private String password;
    private String name;
    private String nickname;
    private Date birthDate;
    private String gender; // ENUM("M", "F")
    private String profileFilename;
    private String authProvider;
    private Integer level;
    private Integer experiencePoints;
    private Integer studyDifficulty;
    private String currentSchool; // ENUM("초등","중등")
    private Integer currentGrade;
    private Integer hintCount;
    private String isDeleted; // ENUM("N", "Y")
    private Date createdDate;
    private Date updatedDate;
    private Integer maxExp; // 레벨업에 필요한 누적 경험치
    private List<String> roleNames; // 사용자 권한 정보 목록

    public String getProfileFileUrl() {
        if (profileFilename == null || profileFilename.isBlank()) {
            return null;
        }
        return "/images/profile/" + userNo + "/" + profileFilename;
    }

}
