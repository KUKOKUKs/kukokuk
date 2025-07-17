package com.kukokuk;

import com.kukokuk.service.QuizService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@MapperScan("com.kukokuk.mapper")
public class KukokukApplication {

	public static void main(String[] args) {
		// 1. SpringBoot 실행 → ApplicationContext 반환됨
		ApplicationContext context = SpringApplication.run(KukokukApplication.class, args);

		// 2. QuizService를 Bean으로부터 가져오기
		QuizService quizService = context.getBean(QuizService.class);

		// 3. 퀴즈 100개 자동 생성 메소드 호출
		quizService.insertRandomQuizBulk(100);

		System.out.println("✅ 퀴즈 100개 자동 생성 완료");
	}
}
