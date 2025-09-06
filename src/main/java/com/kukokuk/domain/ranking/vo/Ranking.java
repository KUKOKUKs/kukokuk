package com.kukokuk.domain.ranking.vo;

import com.kukokuk.domain.user.vo.User;
import com.kukokuk.domain.group.vo.Group;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * 랭킹 테이블 매핑 VO
 * 사용자별 컨텐츠 최고 점수 및 전체/그룹 랭킹 정보
 */
@Getter
@Setter
@Alias("Ranking")
public class Ranking {

    private int rankNo;                    // 랭킹 레코드 고유번호
    private String contentType;            // 컨텐츠 종류 (SPEED, LEVEL, DICTATION, TWENTY, STUDY, ESSAY)
    private int playCount;                 // 해당 컨텐츠 플레이 횟수
    private BigDecimal totalScore;         // 최종 점수 (BASE_SCORE + TIME_BONUS)
    private int userNo;                    // 사용자 번호
    private Integer groupNo;               // 그룹 번호 (NULL: 전체랭킹, 값있음: 그룹랭킹)
    private Date createdDate;              // 최초 랭킹 등록일
    private Date updatedDate;              // 점수 갱신일

    private User user;                     // 사용자 정보
    private Group group;                   // 그룹 정보 (그룹 랭킹인 경우)

    // 랭킹 조회시 추가 정보
    private Integer rank;                  // 실제 순위 (1위, 2위, 3위...)
    private String userNickname;           // 사용자 닉네임 (조인 없이 바로 사용)
    private String groupTitle;             // 그룹명 (조인 없이 바로 사용)
}