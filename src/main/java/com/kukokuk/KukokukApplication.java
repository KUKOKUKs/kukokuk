package com.kukokuk;

import com.kukokuk.service.DictationService;
import com.kukokuk.service.QuizService;
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

       /*// QuizService Bean 꺼내기
        QuizService quizService = context.getBean(QuizService.class);

        DictationService dictationService = context.getBean(DictationService.class);

        dictationService.insertGenerateAiQuestions(100);*/
     /*   // 퀴즈 유형별 100개씩 생성
        quizService.insertRandomQuizBulk(200);*/
    }
}
