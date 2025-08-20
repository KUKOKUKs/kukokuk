package com.kukokuk.common.util;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class FileValidationUtils {

    // 파일 확장자 추출
    public static String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("파일 이름이 잘못되었습니다.");
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    // 파일 유효성 검사
    public static void validateProfileImage(MultipartFile file) {
        // 파일 유무 체크
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 선택되지 않았습니다.");
        }

        // 파일 크기 제한
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("이미지 크기는 5MB를 초과할 수 없습니다.");
        }

        // MIME 타입 검사
        String contentType = file.getContentType();
        List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp", "image/heic");

        if (!allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않은 이미지 형식입니다.");
        }

        // 파일명 확장자 점검
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.matches(".*\\.(jpg|jpeg|png|webp|heic)$")) {
            throw new IllegalArgumentException("허용되지 않은 확장자입니다.");
        }
    }

}
