package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

  @Getter
  @Setter
  @Alias("User")
  public class User {
    private int userNo;
    private String username;
    private String password;
    private String name;
    private String nickname;
    private Date birthDate;
    private String gender; // ENUM("M", "F")
    private String profileFilename;
    private String authProvider;
    private int level;
    private String experiencePoints;
    private int studyDifficulty;
    private String currentSchool; // ENUM("초","중")
    private int currentGrade;
    private String isDeleted; // ENUM("N", "Y")
    private Date createdDate;
    private Date updatedDate;
  }
