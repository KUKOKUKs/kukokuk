package com.kukokuk.domain.edunet;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.domain.study.dto.ParseMaterialRequest;
import com.kukokuk.domain.study.dto.ParseMaterialResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/studies/edunet")
@RequiredArgsConstructor
public class ApiEdunetUrlController {

    private final EdunetUrlParseService edunetUrlParseService;

    /**
     *
     *      * POST /api/admin/studies/parse-materials
     *      * 에듀넷 경로를 전달하면 비동기 큐로 파싱작업을 수행한 후 원본데이터를 DB에 저장하는 API
     *      * 요청 바디 : { urls : [에듀넷 url 경로 리스트]}
     *      * 응답 바디 : { skippedUrls : 스킵된 작업(중복 url) , enqueuedUrls : 큐에 저장되어 진행예정인 작업}
     *
     */
    @PostMapping("/parse-materials")
    public ResponseEntity<ApiResponse<ParseMaterialResponse>> parseMaterialByEdunetUrl(@RequestBody
    ParseMaterialRequest request) {

        // 파이썬 서버를 호출하는 서비스 메소드 호출
        ParseMaterialResponse parseMaterialResponse = edunetUrlParseService.createMaterial(request);

        // 전달받은 응답데이터를 응답통일 객체의 data부분에 설정
        ApiResponse<ParseMaterialResponse> apiResponse = ApiResponse.success("요청성공",
            parseMaterialResponse);

        return ResponseEntity
            .ok()
            .body(apiResponse);
    }
}
