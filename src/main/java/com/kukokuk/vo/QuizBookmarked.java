package com.kukokuk.vo;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@NoArgsConstructor
@Alias("BookMarked")
public class QuizBookmarked {

    private Integer bookmarkNo;     // 북마크 식별자 (PK)
    private Integer userNo;         // 사용자 번호 (FK)
    private Integer quizNo;         // 퀴즈 번호 (FK)
    private LocalDateTime createdDate; // 북마크 생성일시
}
