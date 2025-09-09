package com.kukokuk.integration.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkerLauncher {

    private final DailyStudyWorker worker;

    //  @PostConstruct - Spring Bean이 초기화된 직후에 실행됨
    // 즉, 서버가 켜지면 자동으로 studyGenerateWorker() 호출
    @PostConstruct
    public void init() {
        worker.studyGenerateWorker();
    }
}
