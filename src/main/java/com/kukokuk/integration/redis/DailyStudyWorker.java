package com.kukokuk.integration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.common.dto.JobStatusResponse;
import com.kukokuk.common.store.RedisJobStatusStore;
import com.kukokuk.domain.study.dto.DailyStudyJobPayload;
import com.kukokuk.domain.study.dto.DailyStudySummaryResponse;
import com.kukokuk.domain.study.dto.UserStudyRecommendationDto;
import com.kukokuk.domain.study.service.StudyService;
import com.kukokuk.domain.study.vo.DailyStudy;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class DailyStudyWorker {

    private final StudyService studyService;

    @Async("aiTaskExecutor")
    public void generateStudyAsync(DailyStudyJobPayload payload) {
        studyService.generateStudy(payload);  // Service 로직 호출
    }
}
