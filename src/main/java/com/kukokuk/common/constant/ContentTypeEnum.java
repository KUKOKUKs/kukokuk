package com.kukokuk.common.constant;

import lombok.Getter;

/**
 * 오타 방지, 유지보수 용이, 가독성 향상, 타입 안전성을 위한 컨텐츠 타입 ENUM객체(enum 메소드 활용 가능)
 * <p>
 *     컨텐츠 타입이 추가될 경우 ContentTypeEnum 객체에 추가하여 활용
 */
@Getter
public enum ContentTypeEnum {

    SPEED("스피드 퀴즈")
    ,LEVEL("단계별 퀴즈")
    ,DICTATION("받아쓰기")
    ,TWENTY("스무고개")     // 그룹 전용
    ,STUDY("일일 학습")
    ,ESSAY("서술형 학습")
    ;

    private final String description;

    ContentTypeEnum(String description) {
        this.description = description;
    }

    // 문자열 contentType으로 Enum 조회 후 description 반환
    public static String getDescriptionByType(String contentType) {
        if (contentType == null) return null; // null 처리
        try {
            return ContentTypeEnum.valueOf(contentType.toUpperCase()).getDescription();
        } catch (IllegalArgumentException e) {
            return null; // 매칭되는 Enum 없으면 null 반환
        }
    }
    
}
