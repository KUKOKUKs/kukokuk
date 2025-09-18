package com.kukokuk.integration.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkerLauncher {

    private final DailyStudyWorker worker;

}
