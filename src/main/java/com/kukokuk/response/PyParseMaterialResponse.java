package com.kukokuk.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * PYTHON 서버에 edunet/parse-materials 요청으로 응답받은 데이터를 담는 responseDTO
 */
@Getter
@Setter

public class PyParseMaterialResponse {

    private String content;
    private String school;
    private int grade;
    private String title;
    private String keywords;
    private String sourceFilename;

}
