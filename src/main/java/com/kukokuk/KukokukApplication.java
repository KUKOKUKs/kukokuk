package com.kukokuk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@MapperScan("com.kukokuk.mapper")
@EnableScheduling
public class KukokukApplication {

    public static void main(String[] args) {
//      1. SpringBoot 실행 → ApplicationContext 반환됨
        ApplicationContext context = SpringApplication.run(KukokukApplication.class, args);
//
//        // 2. QuizService를 Bean으로부터 가져오기
//        QuizService quizService = context.getBean(QuizService.class);
//        quizService.insertRandomTypeQuizBulk(20);
//        int quizCount = quizService.getQuizCount(20);
//
//        if (100 > quizCount) {
//            // 3. 퀴즈 100개 자동 생성 메소드 호출
//            quizService.insertRandomQuizBulk(100-quizCount);
//
//            System.out.println("퀴즈 "+(100-quizCount)+"개 생성 완료");
//        }
/*
        퀴즈생성 테스트 코드입니다.
 */
    }
}


