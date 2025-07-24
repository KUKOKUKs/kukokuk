package com.kukokuk;

import com.kukokuk.service.QuizService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class KukokukApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(KukokukApplication.class, args);

        // QuizService Bean 꺼내기
        QuizService quizService = context.getBean(QuizService.class);

        // 퀴즈 유형별 100개씩 생성 (기존 퀴즈 중 usageCount 이상인 것은 제외됨)
        quizService.insertRandomTypeQuizBulk(20); // usageCount 0 이상 → 모든 퀴즈 고려
    }
}
