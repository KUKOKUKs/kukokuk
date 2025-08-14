package com.kukokuk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.kukokuk.mapper")
@EnableScheduling
public class KukokukApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(KukokukApplication.class, args);

        // Bean 꺼내기
//        QuizService quizService = context.getBean(QuizService.class);
//        DictationService dictationService = context.getBean(DictationService.class);

        // 받아쓰기 문제 생성
//        dictationService.insertGenerateAiQuestions(3);
//
        // 퀴즈 유형별 100개씩 생성
//        quizService.insertRandomQuizBulk(200);
    }
}
