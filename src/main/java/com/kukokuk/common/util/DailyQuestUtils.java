package com.kukokuk.common.util;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DailyQuestUtils {

    // 컨텐츠 타입으로 해당 퀘스트 수행 링크 반환
    public static String getQuestLinkByContentType(String type) {
        log.info("getQuestLinkByContentType() 실행 type: {}",  type);
        return switch (type.toUpperCase()) {
            case "STUDY", "ESSAY" -> "/study";
            case "SPEED", "LEVEL", "DICTATION" -> "/quiz";
            default -> "/";
        };
    }

}
