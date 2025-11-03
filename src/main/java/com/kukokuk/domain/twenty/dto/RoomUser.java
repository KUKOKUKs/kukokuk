package com.kukokuk.domain.twenty.dto;

import com.kukokuk.common.util.FilePathUtil;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Alias("RoomUser")
public class RoomUser {

    private int userNo;
    private String status;
    private String nickname;
    private String username;                    // 이메일
    private String password;                    // 비밀번호
    private String name;                        // 이름
    private Date birthDate;                     // 생년월일
    private String gender;                      // 성별 ENUM("M", "F")
    private String profileFilename;             // 프로필 이미지 파일명
    private String authProvider;                // 제 3자 로그인 시 소셜 종류
    private Integer level;                      // 레벨
    private Integer experiencePoints;           // 누적 경험치
    private Integer studyDifficulty;            // 학습 단계
    private String currentSchool;               // 학교 ENUM("초등","중등")
    private Integer currentGrade;               // 학년
    private Integer hintCount;                  // 힌트 개수
    private String isDeleted;                   // ENUM("N", "Y")
    private Date createdDate;
    private Date updatedDate;

    // 프로필 이미지 경로 생성
    public String getProfileFileUrl() {
        return FilePathUtil.getProfileImagePath(userNo, profileFilename);
    }
}
