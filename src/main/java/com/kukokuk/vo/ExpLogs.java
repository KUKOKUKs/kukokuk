package com.kukokuk.vo;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@Alias("ExpLogs")
public class ExpLogs {

    private int expLogNo;           // 경험치 로그 번호
    private int userNo;             // 경험치 획득한 사용자
    private String contentType;     // 경험치 획득한 컨텐츠
    private int contentNo;          // 경험치 획득한 컨텐츠 테이블의 식별자값
    private int expGained;          // 획득한 경험치량
    private Date createdDate;       // 생성일자

}
