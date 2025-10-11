package com.kukokuk.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {

    private final AmazonS3 s3Client;

    @Value("${nhn.s3.bucket}") // application.yml에서 버킷 이름 주입
    private String bucket;

    @Value("${nhn.s3.tenant-id}") // application.yml에서 버킷 이름 주입
    private String tenantId;

    @Value("${nhn.s3.endpoint}") // application.yml에서 버킷 이름 주입
    private String endpoint;

    public String uploadGroupMaterial(MultipartFile file, int groupNo) throws IOException {
        // 1. 파일 확장자 추출 (.hwp, .hwpx, .pdf 등))
        String ext = file.getOriginalFilename()
            .substring(file.getOriginalFilename().lastIndexOf("."));

        // 2. 업로드할 객체의 key 생성 (이 키로 파일이 저장됨)
        // materials/{groupNo}/{랜덤UUID}.확장자
        // 원본파일명은 DB에 저장하고 키에는 사용하지 않음
        String key = "materials/" + groupNo + "/" + UUID.randomUUID() + ext;

        // 3. Object Storage에 저장할 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());     // 파일 크기 (Content-Length)
        metadata.setContentType(file.getContentType()); // 파일 MIME 타입 (예: application/haansoft-hwp)

        // 4. S3 클라이언트로 실제 업로드 수행
        // bucket: 저장할 버킷 이름
        // key: 버킷 내의 경로
        // file.getInputStream(): 파일 데이터 스트림2
        // metadata: 파일 정보 (길이, 타입 등)
        s3Client.putObject(bucket, key, file.getInputStream(), metadata);

        // 5. 업로드된 객체의 접근 URL 반환
        // (버킷이 public이면 그대로 접근 가능, private이면 presigned URL 필요)
        String fullUrl = String.format(
            "%s/v1/%s/%s/%s",
            endpoint, tenantId, bucket, key
        );

        return fullUrl;
    }

    public String uploadAdminMaterial(MultipartFile file, String school, int grade) throws IOException {
        // 1. 파일 확장자 추출 (.hwp, .hwpx, .pdf 등))
        String ext = file.getOriginalFilename()
            .substring(file.getOriginalFilename().lastIndexOf("."));

        // 2. 업로드할 객체의 key 생성 (이 키로 파일이 저장됨)
        // ex) materials/admin/서울초등학교/3/UUID.hwpx
        String key = String.format("materials/admin/%s/%d/%s%s", school, grade, UUID.randomUUID(), ext);

        // 3. Object Storage에 저장할 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());     // 파일 크기 (Content-Length)
        metadata.setContentType(file.getContentType()); // 파일 MIME 타입 (예: application/haansoft-hwp)

        // 4. S3 클라이언트로 실제 업로드 수행
        // bucket: 저장할 버킷 이름
        // key: 버킷 내의 경로
        // file.getInputStream(): 파일 데이터 스트림2
        // metadata: 파일 정보 (길이, 타입 등)
        s3Client.putObject(bucket, key, file.getInputStream(), metadata);

        // 5. 업로드된 객체의 접근 URL 반환
        // (버킷이 public이면 그대로 접근 가능, private이면 presigned URL 필요)
        String fullUrl = String.format(
            "%s/v1/%s/%s/%s",
            endpoint, tenantId, bucket, key
        );

        return fullUrl;
    }

}
