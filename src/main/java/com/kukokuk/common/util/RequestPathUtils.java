package com.kukokuk.common.util;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass // 자동으로 private 생성자 + 모든 메서드 static 처리
public class RequestPathUtils {

    /**
     * Referer에서 path + query 반환
     * Referer 없으면 "/" 반환
     */
    public String getRefererPathWithQuery(HttpServletRequest request) {
        log.info("getRefererPathWithQuery() 유틸 메소드 실행");
        String path = getRefererPath(request);
        String query = getRefererQuery(request);
        return query.isEmpty() ? path : path + "?" + query;
    }

    /**
     * Referer에서 path만 반환
     * Referer 없으면 루트 "/" 반환
     */
    public String getRefererPath(HttpServletRequest request) {
        log.info("getRefererPath() 유틸 메소드 실행");

        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) return "/";

        try {
            // URI.create()는 런타임 발생으로 URISyntaxException 예외 발생시키기 위해 new로 생성
            URI uri = new URI(referer);
            return uri.getPath() != null ? uri.getPath() : "/";
        } catch (URISyntaxException e) {
            return "/";
        }
    }

    /**
     * Referer에서 query만 반환
     * Referer 없으면 "" 반환
     */
    public String getRefererQuery(HttpServletRequest request) {
        log.info("getRefererQuery() 유틸 메소드 실행");

        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) return "";

        try {
            // URI.create()는 런타임 발생으로 URISyntaxException 예외 발생시키기 위해 new로 생성
            URI uri = new URI(referer);
            return uri.getQuery() != null ? uri.getQuery() : "";
        } catch (URISyntaxException e) {
            return "";
        }
    }

}
