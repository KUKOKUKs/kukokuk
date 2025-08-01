package com.kukokuk.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        // ModelMapper 객체를 생성한다.
        ModelMapper mapper = new ModelMapper();

        // 생성된 ModelMapper 객체에 설정을 추가하기 위해 Configuration 획득
        mapper.getConfiguration()
            // 매핑 전략을 설정한다. 프로퍼티 이름이 일치할 때만 매핑한다.
            .setMatchingStrategy(MatchingStrategies.STRICT)
            // null 값은 skip 한다.
            .setSkipNullEnabled(true);

        return mapper;
    }

    // dotenv 환경설정
    @Bean
    public Dotenv dotenv() {
        // .env 파일을 읽어서 환경변수로 사용
        return Dotenv.configure().directory("./")
            .load();
    }
}
