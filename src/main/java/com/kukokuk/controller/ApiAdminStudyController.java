package com.kukokuk.controller;

import com.kukokuk.request.ParseMaterialRequest;
import com.kukokuk.response.ApiResponse;
import com.kukokuk.response.ParseMaterialResponse;
import com.kukokuk.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/studies")
@RequiredArgsConstructor
public class ApiAdminStudyController {

  private final StudyService studyService;

  /**
   *
   * @return
   */
  @PostMapping("/parse-materials")
  public ResponseEntity<ApiResponse<ParseMaterialResponse>> parseMaterialByEdunetUrl(@RequestBody
      ParseMaterialRequest request){

    // 파이썬 서버를 호출하는 서비스 메소드 호출
    ParseMaterialResponse parseMaterialResponse = studyService.createMaterial(request);

    // 전달받은 응답데이터를 응답통일 객체의 data부분에 설정
    ApiResponse<ParseMaterialResponse> apiResponse = ApiResponse.success("요청성공", parseMaterialResponse);

    return ResponseEntity
        .ok()
        .body(apiResponse);
  }
}
