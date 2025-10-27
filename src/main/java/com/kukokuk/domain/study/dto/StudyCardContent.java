package com.kukokuk.domain.study.dto;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyCardContent {

    private String type;    // paragraph, table, example, definition, quote, list
    private Object content; // String, List<String>, List<List<String>> 중 하나

    // 문자열 타입 여부
    public boolean isStringType() {
        return "paragraph".equals(type)
            || "example".equals(type)
            || "definition".equals(type)
            || "quote".equals(type);
    }

    // table 타입 여부
    public boolean isTable() {
        return "table".equals(type) && content instanceof List;
    }

    // list 타입 여부
    public boolean isList() {
        return "list".equals(type) && content instanceof List && !isTable();
    }

    /** paragraph, example, definition, quote 타입에서 문자열 반환 */
    public String asString() {
        return isStringType() && content != null ? content.toString() : "";
    }

    // list 타입에서 1차원 배열 반환
    @SuppressWarnings("unchecked")
    public List<String> asList() {
        if (isList()) {
            try {
                return (List<String>) content;
            } catch (ClassCastException e) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    // table 타입에서 2차원 배열 반환
    @SuppressWarnings("unchecked")
    public List<List<String>> asTable() {
        if (isTable()) {
            try {
                return (List<List<String>>) content;
            } catch (ClassCastException e) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    // table 타입에서 헤더(첫 번째 행) 반환
    public List<String> tableHeader() {
        List<List<String>> table = asTable();
        if (!table.isEmpty()) {
            return table.get(0);
        }
        return Collections.emptyList();
    }

    // table 타입에서 바디(두 번째 행 이후) 반환
    public List<List<String>> tableBody() {
        List<List<String>> table = asTable();
        if (table.size() > 1) {
            return table.subList(1, table.size());
        }
        return Collections.emptyList();
    }

}
