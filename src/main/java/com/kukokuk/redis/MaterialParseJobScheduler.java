package com.kukokuk.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.exception.AppException;
import com.kukokuk.mapper.DailyStudyMaterialMapper;
import com.kukokuk.mapper.MaterialParseJobMapper;
import com.kukokuk.response.ApiResponse;
import com.kukokuk.response.PyParseMaterialResponse;
import com.kukokuk.vo.DailyStudyMaterial;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
@Slf4j // 로그를 위한 어노테이션
public class MaterialParseJobScheduler {

  private final StringRedisTemplate stringRedisTemplate;
  private final MaterialParseJobMapper materialParseJobMapper;
  private final DailyStudyMaterialMapper dailyStudyMaterialMapper;

  @Value("${pyserver.url}")
  private String pyserverUrl;

  /**
   * 1. @Scheduled로 주기적으로 Redis 큐에서 leftPop()
   * 2. ObjectMapper로 파싱 → jobNo, url 추출
   * 3. Job 상태 IN_PROGRESS로 변경
   * 4. Python 서버에 요청 보내고 응답받음
   * 5. 응답 결과를 DB에 저장 (dailyStudyMaterial)
   * 6. 성공 시 MaterialParseJob 상태 SUCCESS로 변경
   * 7. 예외 시 MaterialParseJob 상태 FAILED로 변경 + 로그
   */
  @Scheduled(fixedDelay = 10000) // 5초마다 큐 확인
  public void consumeQueue() {
    // redis 리스트에 저장된 데이터를 하나씩 뽑아옴
    String payload = stringRedisTemplate.opsForList().leftPop("parse:queue");

    if (payload == null) {
      return;
    }
    // payload : {"jobNo":3,"url":"https://www.edunet.net/clssStdDt/view/150/2085879?sbjtClsf=88922&srvcClsf=59598"}

    int jobNo = 0;
    try {
      // 1. JSON 파싱하기
      ObjectMapper objectMapper = new ObjectMapper();
      // objectMapper.readTree()는 파라미터로 전달되는 JSON 구조에 맞는 JsonNode의 하위클래스를 반환함
      // Ex) { "name": "다영" } -> OnjectNode, [1, 2, 3] -> ArrayNode
      JsonNode node = objectMapper.readTree(payload); // Checked Exception이므로 예외처리 필수

      jobNo = node.get("jobNo").asInt();
      String url = node.get("url").asText();

      // 2. jobId로 해당 job의 status를 IN_PROGRESS로 업데이트하기
      // - MaterialParseJob를 업데이트
      materialParseJobMapper.updateParseJobStatusToInProgress(jobNo);

      // 3. 파이썬 서버에 POST API 요청 전송하기
      // 요청 데이터와 응답데이터를 JSON으로 전달하므로, DTO혹은 Map 타입으로 전달하기
      Map<String, Object> pyRequestBody = Map.of(
          "url", url
      );
      RestClient restClient = RestClient.create(); // 기본 설정으로 초기화된 새로운 RestClient 인스턴스 생성

      // py서버에 요청 전송
      // 응답데이터는 ApiResponse<PyParseMaterialResponse>에 바인딩된다
      ResponseEntity<ApiResponse<PyParseMaterialResponse>> pyResponseEntity = restClient.post()
          .uri(pyserverUrl + "/edunet/parse-materials")
          .contentType(MediaType.APPLICATION_JSON)
          .body(pyRequestBody) // Java 객체를 자동으로 JSON으로 변환해준다
          .retrieve()
          // 설정된 요청을 실행하고, 응답을 처리할 수 있는 RestClient.ResponseSpec 객체를 반환
          .toEntity(new ParameterizedTypeReference<ApiResponse<PyParseMaterialResponse>>() {
          });
          /* ParameterizedTypeReference : 제네릭타입<T>은 원래 런타입에 타입이 사라짐
            제네릭타입 정보를 런타임까지 보존해서, 제네릭 타입의 응답에 JSON이 정확히 매핑될 수 있도록 한다
           */

      // ResponseEntity에서 body만 추출
      ApiResponse<PyParseMaterialResponse> pyResponseBody = pyResponseEntity.getBody();
      // body에서 data필드 추출
      PyParseMaterialResponse data = pyResponseBody.getData();

      // 4. 응답받은 자료의 학교, 학년으로 다음 시퀀스 값 조회
      int lastSequence = dailyStudyMaterialMapper.getMaxSequenceBySchoolAndGrade(data.getSchool(), data.getGrade());
      int sequence = lastSequence + 1;

      // 5. 응답으로 반환받은 데이터를 dailyStudyMaterial테이블에 저장
      DailyStudyMaterial dailyStudyMaterial = DailyStudyMaterial.builder()
          .content(data.getContent())
          .grade(data.getGrade())
          .school(data.getSchool())
          .materialTitle(data.getTitle())
          .keywords(data.getKeywords())
          .sourceFilename(data.getSourceFilename())
          .build();
      // sequence 값은 해당 school과 grade로 mapper내에서 계산해서 저장됨
      dailyStudyMaterialMapper.insertStudyMaterial(dailyStudyMaterial);

      // 6. jobId로 해당 job의 status를 SUCCESS로 업데이트하기
      // - MaterialParseJob를 업데이트
      materialParseJobMapper.updateParseJobStatusToSuccess(jobNo);

    } catch (Exception e) {
      materialParseJobMapper.updateParseJobStatusToFailed(jobNo, e.getMessage()); // 에러 메시지 저장
      log.error("파싱 실패 - jobNo {}: {}", jobNo, e.getMessage(), e);
    }

  }
}
