package com.kukokuk.common.dto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/*
 * 정보의 목록을 표현하는 DTO 클래스
 * items: 표현할 데이터
 * condition: 검색 조건
 * pagination: 페이징 정보
 */
@Getter
@Setter
public class Page<T> {

    private List<T> items;
    private Map<String, Object> condition;
    private Pagination pagination;

    /**
     * condition(Map)을 쿼리스트링 형태로 반환하되 page, offset, rows 등은 제외
     * <p>
     *     &로 시작하여 key=value&key=value...
     */
    public String getConditionQuery() {
        // 비었거나 없을 경우 빈값 반환
        if (condition == null || condition.isEmpty()) {
            return "";
        }

        // 제외할 키워드 목록 정의(추가될 수도 있기 떄문에)
        Set<String> excludeKeys = Set.of("page", "offset", "rows");

        return condition.entrySet().stream()
            .filter(e -> e.getValue() != null && !e.getValue().toString().isBlank())  // 값이 없는 항목 제외
            .filter(e -> !excludeKeys.contains(e.getKey())) // 제외 조건
            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue().toString(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&")); // & 로 연결
    }

}

