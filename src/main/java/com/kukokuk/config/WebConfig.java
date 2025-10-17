package com.kukokuk.config;

import com.kukokuk.common.util.FilePathUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
    Spring MVC 설정을 커스터마이징하기 위해 WebMvcConfigurer 인터페이스 구현
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    /*
        정적 리소스 핸들러를 추가하는 메서드 오버라이드
        클라이언트 브라우저에서 http://localhost:8080/images/profile/1/abc.jpg 같은 URL로 요청하면,
        실제 서버의 로컬 파일 시스템에 저장된 "C:/kukokuk/user/profileImage/1/abc.jpg" 파일을 찾아서 응답하도록 설정
    */

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "/images/profile/**" 패턴의 모든 URL 요청에 대해 이 리소스 핸들러가 동작
        // (/images/profile/ 는 사용자 프로필 이미지 경로)
        registry.addResourceHandler(FilePathUtil.PROFILE_DIR + "**")
            // 실제 파일을 읽어올 물리적 경로(디렉토리)를 지정
            // "file:///"는 로컬 파일 시스템임을 의미하는 스킴(scheme)
            // 이 설정으로 스프링 MVC는 "/images/profile/**" 경로로 들어오는 요청을
            // 내부적으로 지정한 로컬 경로에서 파일을 읽어 클라이언트에 응답하는 정적 자원 서빙 기능을 수행
            .addResourceLocations("file:///C:/kukokuk/user/profileImage/");
    }
}