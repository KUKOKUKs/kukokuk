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

    /** paragraph, example, definition, quote 타입에서 문자열 반환 */
    public String asString() {
        return content instanceof String ? (String) content : null;
    }

    // list 타입에서 1차원 배열 반환
    @SuppressWarnings("unchecked")
    public List<String> asList() {
        return content instanceof List && !isTable()
            ? (List<String>) content
            : Collections.emptyList();
    }

    // table 타입에서 2차원 배열 반환
    @SuppressWarnings("unchecked")
    public List<List<String>> asTable() {
        return isTable() ? (List<List<String>>) content : Collections.emptyList();
    }

    // table 타입 여부
    public boolean isTable() {
        return "table".equals(type) && content instanceof List;
    }

    // list 타입 여부
    public boolean isList() {
        return "list".equals(type) && content instanceof List && !isTable();
    }

    // 문자열 타입 여부
    public boolean isStringType() {
        return "paragraph".equals(type)
            || "example".equals(type)
            || "definition".equals(type)
            || "quote".equals(type);
    }

}
