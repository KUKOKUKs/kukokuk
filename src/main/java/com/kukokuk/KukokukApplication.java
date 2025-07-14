package com.kukokuk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kukokuk.repository")
public class KukokukApplication {

	public static void main(String[] args) {
		SpringApplication.run(KukokukApplication.class, args);
	}

}
