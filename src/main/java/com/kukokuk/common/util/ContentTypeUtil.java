package com.kukokuk.common.util;

import java.util.Map;

/**
 * 콘텐츠 타입 관련 유틸 클래스
 */
public class ContentTypeUtil {

    // 변환 매핑 테이블
    private static final Map<String, String> contentTypeMap;
    static {
        contentTypeMap = Map.of(
            "SPEED", "스피드 퀴즈"
            , "LEVEL", "단계별 퀴즈"
            , "DICTATION", "받아쓰기"
            , "TWENTY", "스무고개"
        ); // 불변화: 안전성 향상
    }

    /**
     * 영어 콘텐츠 타입(대소문자 무관) 한글명 반환
     * @param contentType 컨텐츠 타입
     * @return 매핑된 한글명, 매핑 없으면 원본 contentType 반환, null이면 빈 문자열 반환
     */
    public static String getContentTypeName(String contentType) {
        if (contentType == null) return "";
        String key = contentType.toUpperCase();
        return contentTypeMap.getOrDefault(key, contentType);
    }

}
