package com.kukokuk.domain.twenty.dto;

import com.kukokuk.domain.twenty.mapper.TwentyMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * 게임방의 결과를 집어 넣기 위한 DTO
 */
@Getter
@Setter
@Alias("TwentyResult")
@NoArgsConstructor
public class TwentyResult {
    private int roomNo;
    private String answers;
    private String status;
    private Integer winnerNo;  // 얘는 null이 나올 수도 있다.
    private int tryCnt;
    private String title;
    private String isSuccess;
    private String nickName;
    private int participantCount;
}
