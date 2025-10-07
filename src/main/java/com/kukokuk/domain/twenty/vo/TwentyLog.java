package com.kukokuk.domain.twenty.vo;

import com.kukokuk.domain.user.vo.User;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@Alias("TwentyLog")
public class TwentyLog {
    private int logNo;
    private String type;
    private String message;
    private String isSuccess;
    private String answer;
    private Date createdDate;
    private int roomNo;
    private int userNo;

    private User user;
    private TwentyRoom room;
}
