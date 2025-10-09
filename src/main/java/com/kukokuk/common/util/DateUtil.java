package com.kukokuk.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 날짜 관련 공통 유틸리티 클래스
 */
public class DateUtil {

    /**
     * 전달받은 패턴으로 오늘 날짜를 반환
     * @param pattern 날짜 포맷 (예: "yyyy-MM", "yyyyMMdd", "yyyy/MM/dd" 등)
     * @return 포맷팅된 오늘 날짜 문자열
     */
    public static String getToday(String pattern) {
        // 패턴 유효성 검사 (null 또는 빈 문자열이면 기본 포맷 사용)
        if (pattern == null || pattern.isBlank()) {
            pattern = "yyyy-MM-dd";
        }
        LocalDate today = LocalDate.now(); // 현재 날짜 가져오기
        return today.format(DateTimeFormatter.ofPattern(pattern)); // 포맷 적용 후 문자열로 반환
    }

    /**
     * 문자열이 주어진 포맷의 유효한 날짜인지 판별
     * @param dateStr 검사할 날짜 문자열
     * @param pattern 검사할 날짜 포맷
     * @return 유효하면 true, 아니면 false
     */
    public static boolean isValidDate(String dateStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}