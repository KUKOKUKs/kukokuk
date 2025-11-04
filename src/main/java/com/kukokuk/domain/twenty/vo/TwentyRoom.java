package com.kukokuk.domain.twenty.vo;

import com.kukokuk.domain.user.vo.User;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("TwentyRoom")
@NoArgsConstructor
public class TwentyRoom {

    private int roomNo;             // 방 번호
    private String correctAnswer;   // 정답
    private String isSuccess;       // 정답 여부
    private String status;          // 방 상태
    private Date createdDate;       // 생성일
    private Date updatedDate;       // 수정일
    private int groupNo;            // 그룹 번호
    private Integer winnerNo;           // 손들기 or 게임승리자 번호
    private int tryCnt;             // 진행 라운드 횟수
    private String title;           // 방 제목

    private User teacher;           // 교사 정보
}
