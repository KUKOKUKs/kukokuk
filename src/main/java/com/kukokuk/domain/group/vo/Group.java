package com.kukokuk.domain.group.vo;

import com.kukokuk.domain.user.vo.User;
import java.util.Date;
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
@Alias("Group")
public class Group {

    private int groupNo;            // 그룹번호
    private String title;           // 그룹명
    private String password;        // 그룹 비밀번호(숫자로만 4자리) NULL 일 경우 공개 그룹
    private String isDeleted;       // ENUM("N", "Y")
    private Date createdDate;
    private Date updatedDate;

    private User teacher;           // 그룹 생성자(TEACHER 권한) 정보

}
