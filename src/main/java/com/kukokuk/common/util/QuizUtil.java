package com.kukokuk.common.util;

import java.util.ArrayList;
import java.util.List;

public class QuizUtil {

    /**
     * 옵션 문자열들을 배열 형태로 받아서 List로 반환
     * @param options option1, option2, option3, option4 등 순서대로 전달
     * @return 옵션 문자열 리스트
     */
    public static List<String> extractOptions(String... options) {
        List<String> result = new ArrayList<>();
        for (String opt : options) {
            if (opt != null && !opt.isBlank()) {
                result.add(opt);
            }
        }
        return result;
    }

}
