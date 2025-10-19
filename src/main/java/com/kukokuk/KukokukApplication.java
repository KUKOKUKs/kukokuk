package com.kukokuk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.kukokuk.domain")
@EnableScheduling
public class KukokukApplication {

    public static void main(String[] args) {
        SpringApplication.run(KukokukApplication.class, args);
        
        // 아래는 기본 데이터베이스 자료 생성 시 실행
//        ApplicationContext context = SpringApplication.run(KukokukApplication.class, args);

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
