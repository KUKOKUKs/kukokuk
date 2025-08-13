package com.kukokuk.response;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GeminiEssayResponse {
    private List<Section> sections;

    @Getter
    @Setter
    public static class Section {
        private String type;
        private String icon;
        private String title;
        private List<Item> items;

        @Getter
        @Setter
        public static class Item {
            private String text;
            private Map<String, Object> extra;       // 아이콘, 점수, 태그 등 확장 필드

            // JSON을 자바 객체로 변환할 때, 클래스에 없는 필드명이 발견되면 이 메서드가 호출
            // void setExtraField(String key, Object value) 형태여야함.
            // key -> JSON에서의 필드명
            // value -> 해당 필드 값
            @JsonAnySetter // JSON 역직렬화시 DTO에 정의되지 않은 필드들을 한 곳에 모아담기 위해 사용
            public void setExtraField(String key, Object value) {
                if ("text".equals(key)) return; // text 필드는 무시
                if (extra == null) {
                    extra = new java.util.HashMap<>();
                }
                extra.put(key, value);
            }
        }
    }
}
