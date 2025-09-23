package com.kukokuk.domain.twenty.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("RoomUser")
public class RoomUser {

    private int userNo;
    private String status;
    private String nickName;

}
