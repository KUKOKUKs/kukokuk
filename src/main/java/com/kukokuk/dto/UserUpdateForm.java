package com.kukokuk.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateForm {

    private String password;
    private String name;
    private String nickname;
    private Date birthDate;
    private String gender; // ENUM("M", "F")
    private String profileFilename;
    private Integer level;
    private Integer experiencePoints;
    private Integer studyDifficulty;
    private String currentSchool; // ENUM("초등","중등")
    private Integer currentGrade;
    private Integer hintCount;
    private String isDeleted; // ENUM("N", "Y")

}
