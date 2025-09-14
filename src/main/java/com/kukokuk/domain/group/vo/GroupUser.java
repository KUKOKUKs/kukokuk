package com.kukokuk.domain.group.vo;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("GroupUser")
public class GroupUser {

    private int groupNo;
    private int userNo;
    private Date createdDate;

}
