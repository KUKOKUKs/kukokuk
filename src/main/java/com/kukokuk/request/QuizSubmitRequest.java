package com.kukokuk.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmitRequest {
    private int userNo;                                // 사용자 번호
    private int totalTimeSec;                          // 총 소요 시간
    private List<QuizSubmitResultRequest> results;     // 문제별 정답 제출 리스트
}
