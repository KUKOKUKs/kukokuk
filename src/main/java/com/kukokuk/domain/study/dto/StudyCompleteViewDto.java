package com.kukokuk.domain.study.dto;

import com.kukokuk.domain.study.vo.DailyStudyLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudyCompleteViewDto {

    private DailyStudyLog log;
}
