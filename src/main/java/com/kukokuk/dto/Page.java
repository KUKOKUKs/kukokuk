package com.kukokuk.dto;

import java.util.List;
import java.util.Map;
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

}

