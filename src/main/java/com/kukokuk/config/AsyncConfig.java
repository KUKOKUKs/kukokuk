package com.kukokuk.config;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.log4j.Log4j2;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Async 설정 클래스
 * - Spring이 @Async 호출 시 사용할 Executor 를 제공하고,
 *   비동기에서 발생한 uncaught 예외를 처리할 핸들러를 제공하게 함
 */
@Log4j2
@Configuration
@EnableAsync // @Async가 붙은 메서드를 찾고 비동기 실행을 가능하게 함
public class AsyncConfig implements AsyncConfigurer {

    /**
     * aiTaskExecutor 빈 생성
     * - ThreadPoolTaskExecutor를 생성/설정/초기화하여 스레드풀을 만든다
     * - Bean 이름을 명시(다른 Executor와 구분하거나 @Qualifier로 주입할 때 유용)
     *   (@Qualifier("aiTaskExecutor") 이름을 정확히 지정)
     */
    @Bean(name = "aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);                          // 동시에 돌릴 최소 스레드
        ex.setMaxPoolSize(8);                           // 최대 스레드
        ex.setQueueCapacity(100);                       // 대기열(큐 용량)
        ex.setThreadNamePrefix("ai-");                  // 스레드 이름 접두사(로그 구분용)
        ex.setWaitForTasksToCompleteOnShutdown(true);   // 종료 시 스레드풀 내 남은 작업 완료 대기
        ex.setAwaitTerminationSeconds(300);             // 최대 5분 대기(ai api로 학습 재생성시 최대 시간 예상)

        // 스레드풀과 큐가 모두 꽉 찼을 때 처리 정책
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        ex.initialize(); // 스레드풀 초기화
        return ex;
    }

    /**
     * Spring이 @Async로 실행할 때 어떤 스레드풀(Executor) 을 쓸지 알려주는 메서드
     * (여기에 반환한 Executor에서 비동기 작업들이 실행)
     */
    @Override
    public Executor getAsyncExecutor() {
        return aiTaskExecutor();
    }

    /**
     * '@Async'로 실행된 void(또는 Future를 반환하지 않는) 메서드에서 예외가 발생하고
     * 그 예외를 메서드 내부에서 잡지 않았을 때(uncaught) 호출되는 후처리기
     * (주로 로그/모니터링/알림 용도. 사용자 응답용은 아님)
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (e, m, params)
            -> log.error("Async error in {} params={}", m, Arrays.toString(params), e);
    }

}
