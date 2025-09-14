package com.kukokuk.domain.dictation.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictationResultSummaryDto {

    private int totalQuestion;
    private int correctAnswers;
    private double totalTimeSec;
    private double averageTimePerQuestion;
    private List<DictationResultsDto> results;

}
