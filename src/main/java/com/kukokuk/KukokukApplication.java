package com.kukokuk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.kukokuk.mapper")
@EnableScheduling
public class KukokukApplication {

	public static void main(String[] args) {
		SpringApplication.run(KukokukApplication.class, args);
	}

}
