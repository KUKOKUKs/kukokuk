package com.kukokuk.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    // application.yml 에서 값을 주입받음
    @Value("${nhn.s3.endpoint}")   // NHN Object Storage S3 호환 엔드포인트 URL
    private String endpoint;

    @Value("${nhn.s3.access-key}") // NHN 콘솔에서 발급받은 Access Key
    private String accessKey;

    @Value("${nhn.s3.secret-key}") // NHN 콘솔에서 발급받은 Secret Key
    private String secretKey;

    @Value("${nhn.s3.region-name}")
    private String region;

    @Bean // 스프링 컨텍스트에 AmazonS3 객체를 빈으로 등록
    public AmazonS3 amazonS3() {
        // 1. AWS 인증 객체 생성 (AccessKey + SecretKey)
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        // 2. AmazonS3 클라이언트 빌더를 사용해 클라이언트 생성
        return AmazonS3ClientBuilder.standard()
            // 인증정보 주입 (AWSStaticCredentialsProvider를 사용해야 함)
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            // NHN Object Storage 엔드포인트 + 리전 설정
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(endpoint, region)
            )
            // NHN Object Storage는 "Path-Style" 접근만 지원 → https://endpoint/bucket/key 형식
            // AWS S3는 Virtual Host 스타일 -> https://{bucket}.s3.amazonaws.com/{objectKey}
            .enablePathStyleAccess()
            // S3 SDK는 파일 업로드 시 Transfer-Encoding: chunked 방식이 기본
            // 일부 S3 호환 스토리지는 chunked encoding을 지원하지 않음
            // 이 옵션 사용시 SDK가 Content-Length를 먼저 계산해서 한 번에 전송 -> 업로드 오류 발생 가능성 적음
            .disableChunkedEncoding()
            .build();
    }

}
