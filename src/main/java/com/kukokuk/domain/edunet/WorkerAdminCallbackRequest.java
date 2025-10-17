package com.kukokuk.domain.edunet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * PYTHON 서버에 edunet/parse-materials 요청으로 응답받은 데이터를 담는 response Dto
 */
@Getter
@Setter
@RequiredArgsConstructor
public class WorkerAdminCallbackRequest {
    private int jobNo;
    private String content;
    private String school;
    private int grade;
    private String title;
    private String keywords;
    private String sourceFilename;

    // 실패 콜백용
    private String error;
}
