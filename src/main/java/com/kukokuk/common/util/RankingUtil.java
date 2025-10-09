package com.kukokuk.common.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 랭킹 관련 공통 유틸리티 클래스
 *
 * 경로: src/main/java/com/kukokuk/common/util/RankingUtil.java
 */
public class RankingUtil {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 현재 년월 반환 (YYYY-MM)
     */
    public static String getCurrentMonth() {
        return LocalDate.now().format(MONTH_FORMATTER);
    }

    /**
     * 날짜 문자열이 당월인지 확인
     * @param rankMonth 확인할 월 (YYYY-MM)
     * @return 당월이면 true
     */
    public static boolean isCurrentMonth(String rankMonth) {
        return getCurrentMonth().equals(rankMonth);
    }

    /**
     * 날짜 문자열이 현재보다 미래인지 확인
     * @param rankMonth 확인할 월 (YYYY-MM)
     * @return 미래 날짜면 true
     */
    public static boolean isFutureMonth(String rankMonth) {
        try {
            YearMonth inputMonth = YearMonth.parse(rankMonth, MONTH_FORMATTER);
            YearMonth currentMonth = YearMonth.now();
            return inputMonth.isAfter(currentMonth);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 날짜 문자열 유효성 검증
     * @param rankMonth 검증할 월 (YYYY-MM)
     * @return 유효하면 true
     */
    public static boolean isValidMonth(String rankMonth) {
        try {
            YearMonth.parse(rankMonth, MONTH_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}