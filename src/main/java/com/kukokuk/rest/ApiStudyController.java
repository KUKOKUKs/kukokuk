package com.kukokuk.rest;

import com.kukokuk.dto.UserStudyRecommendationDto;
import com.kukokuk.request.ParseMaterialRequest;
import com.kukokuk.response.ApiResponse;
import com.kukokuk.response.DailyStudySummaryResponse;
import com.kukokuk.response.ParseMaterialResponse;
import com.kukokuk.response.ResponseEntityUtils;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.service.StudyService;
import com.kukokuk.vo.DailyStudy;
import com.kukokuk.vo.DailyStudyLog;
import com.kukokuk.vo.DailyStudyMaterial;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class ApiStudyController {

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
    ApiResponse<ParseMaterialResponse> apiResponse = ApiResponse.success(parseMaterialResponse);

    return ResponseEntity
        .ok()
        .body(apiResponse);
  }

  /**
   *
   * @return
   */
  @PostMapping()
  public ResponseEntity<ApiResponse> createStudy(){

    studyService.createDailyStudy(3,1);

    return ResponseEntity
        .ok()
        .body(null);
  }

  /**
   * 사용자의 수준, 진도에 맞는 학습자료 목록을 제공하는 API
   * @param securityUser
   * @return
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<DailyStudySummaryResponse>>> getStudiesBUser(
      @RequestParam(defaultValue = "5") int rows,
      @AuthenticationPrincipal SecurityUser securityUser){

    // 사용자의 수준과 진도에 맞는 추천 학습자료(DailyStudy) 목록을 조회하는 메소드 호출
    List<UserStudyRecommendationDto> dtos = studyService.getUserDailyStudies(securityUser.getUser(), rows);

    // UserStudyRecommendationDto에서 응답에 필요한 정보만 반환하도록 ResponseDTO에 매핑
    List<DailyStudySummaryResponse> responses = dtos.stream()
        .map(dto -> {
          DailyStudy study = dto.getDailyStudy();
          DailyStudyLog log = dto.getDailyStudyLog();
          DailyStudyMaterial material = dto.getDailyStudyMaterial();

          int totalCardCount = study.getCardCount();
          int studiedCardCount = (log != null) ? log.getStudiedCardCount() : 0;
          int progressRate = (totalCardCount == 0) ? 0 : (int) ((studiedCardCount * 100.0) / totalCardCount);

          String status = "NOT_STARTED";
          if (log != null) {
            status = log.getStatus(); // "IN_PROGRESS", "COMPLETED" 중 하나라고 가정
          }

          return DailyStudySummaryResponse.builder()
              .dailyStudyNo(study.getDailyStudyNo())
              .title(study.getTitle())
              .cardCount(totalCardCount)
              .status(status)
              .studiedCardCount(studiedCardCount)
              .progressRate(progressRate)
              .school(material.getSchool())
              .grade(material.getGrade())
              .sequence(material.getSequence())
              .build();
        })
        .toList();

    return ResponseEntityUtils.ok("사용자 맞춤 학습자료 목록 조회 성공", responses);
  }
}
