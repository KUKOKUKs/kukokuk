package com.kukokuk.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserStudyLevelForm {

    private int studyDifficulty;
    private String currentSchool; // ENUM("초등","중등")
    private int currentGrade;

}
