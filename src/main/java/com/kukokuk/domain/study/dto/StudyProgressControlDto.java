package com.kukokuk.domain.study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyProgressControlDto {

    private int progressIndex; // 현재 학습 및 퀴즈에 대한 진행 인덱스
    private int totalProgressNum; // 학습 + 퀴즈 총 개수

}
