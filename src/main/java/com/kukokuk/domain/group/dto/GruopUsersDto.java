package com.kukokuk.domain.group.dto;

import com.kukokuk.domain.user.vo.User;
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
@Alias("GroupUsersDto")
public class GruopUsersDto {

    private int groupNo;                // 그룹번호
    private String title;               // 그룹명
    private String motto;               // 급훈
    private Date createdDate;           // 그룹 생성일
    private Date updatedDate;           // 그룹 수정일

    private int teacherNo;               // 그룹 생성자(TEACHER 권한) 번호

    private int memberCount;            // 그룹 인원 수

    private List<User> groupUsers;            // 그룹에 속한 사용자들 정보(userNo, nickname, level, profileFilename)

    // 천 단위 콤마 붙인 getter
    public String getMemberCountFormatted() {
        return String.format("%,d", memberCount);
    }

}
