package com.kukokuk.integration.redis;

import com.kukokuk.domain.study.dto.DailyStudyJobPayload;
import com.kukokuk.domain.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class DailyStudyWorker {

    private final StudyService studyService;

    @Async("aiTaskExecutor")
    public void generateStudyAsync(DailyStudyJobPayload payload) {
        log.info("DailyStudyWorker generateStudyAsync() 실행");
        studyService.generateStudy(payload);  // Service 로직 호출
    }
}
