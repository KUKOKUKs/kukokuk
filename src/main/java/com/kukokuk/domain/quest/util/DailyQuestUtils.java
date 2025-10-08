package com.kukokuk.domain.quest.util;

import com.kukokuk.common.constant.ContentTypeEnum;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DailyQuestUtils {

    // 컨텐츠 타입으로 해당 퀘스트 수행 링크 반환
    public static String getQuestLinkByContentType(ContentTypeEnum type) {
        log.info("getQuestLinkByContentType() 실행 type: {}",  type);
        return switch (type.name()) {
            case "STUDY", "ESSAY" -> "/study";
            case "SPEED", "LEVEL", "DICTATION" -> "/quiz";
            default -> "/";
        };
    }

}
