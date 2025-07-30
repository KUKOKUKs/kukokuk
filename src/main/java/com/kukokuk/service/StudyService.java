package com.kukokuk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.ai.GeminiClient;
import com.kukokuk.ai.GeminiStudyPromptBuilder;
import com.kukokuk.ai.GeminiStudyResponse;
import com.kukokuk.ai.GeminiStudyResponse.Card;
import com.kukokuk.ai.GeminiStudyResponse.EssayQuiz;
import com.kukokuk.ai.GeminiStudyResponse.Quiz;
import com.kukokuk.dto.MainStudyViewDto;
import com.kukokuk.dto.UserStudyRecommendationDto;
import com.kukokuk.mapper.DailyQuestMapper;
import com.kukokuk.mapper.DailyQuestUserMapper;
import com.kukokuk.mapper.DailyStudyCardMapper;
import com.kukokuk.mapper.DailyStudyEssayQuizMapper;
import com.kukokuk.mapper.DailyStudyMapper;
import com.kukokuk.mapper.DailyStudyMaterialMapper;
import com.kukokuk.mapper.DailyStudyQuizMapper;
import com.kukokuk.mapper.MaterialParseJobMapper;
import com.kukokuk.mapper.StudyDifficultyMapper;
import com.kukokuk.request.ParseMaterialRequest;
import com.kukokuk.response.ParseMaterialResponse;
import com.kukokuk.security.SecurityUser;
import com.kukokuk.util.SchoolGradeUtils;
import com.kukokuk.vo.DailyQuest;
import com.kukokuk.vo.DailyQuestUser;
import com.kukokuk.vo.DailyStudy;
import com.kukokuk.vo.DailyStudyCard;
import com.kukokuk.vo.DailyStudyEssayQuiz;
import com.kukokuk.vo.DailyStudyLog;
import com.kukokuk.vo.DailyStudyMaterial;
import com.kukokuk.vo.DailyStudyQuiz;
import com.kukokuk.vo.MaterialParseJob;
import com.kukokuk.vo.StudyDifficulty;
import com.kukokuk.vo.User;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.Log;
import org.modelmapper.internal.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyService {

    private final  DailyStudyMapper dailyStudyMapper;
    private final DailyQuestMapper dailyQuestMapper;
    private final DailyQuestUserMapper dailyQuestUserMapper;
    private final DailyStudyMaterialMapper dailyStudyMaterialMapper;
    private final StudyDifficultyMapper studyDifficultyMapper;
    private final DailyStudyCardMapper dailyStudyCardMapper;
    private final DailyStudyQuizMapper dailyStudyQuizMapper;
    private final DailyStudyEssayQuizMapper dailyStudyEssayQuizMapper;
    private final MaterialParseJobMapper materialParseJobMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final GeminiClient geminiClient;

    private final ObjectMapper objectMapper;

    /*
      메인 화면에 필요한 데이터를 담은 MainStudyViewDto를 반환한다
      <MainStudyViewDto 에 포함되는 데이터>
        1. 학습탭의 일일 도전과제 목록
        2. 사용자의 이전 학습 이력 목록
        3. 사용자_일일 도전과제 목록 (아이템 획득 여부)
     */
    public MainStudyViewDto getMainStudyView(SecurityUser securityUser) {
        MainStudyViewDto dto = new MainStudyViewDto();

        // 1. 학습탭의 일일 도전과제 정보 조회 (인증된 유저와 관련 X)
        List<DailyQuest> dailyQuests = dailyQuestMapper.getDailyQuestByContentType("STUDY");
        dto.setDailyQuests(dailyQuests);

        // 인증된 사용자일때 (인증되지 않은 사용자는 미리 설정한 일일학습 자료 제공 예정)
        if (securityUser != null) { // 수정필요

            User user = securityUser.getUser();

            // 4. 사용자의 이전 학습이력 목록 5개 조회
      /*
      고려할 사항
        1. updatedDate로 정렬
        2. 조회 조건 전달 (개수)
      */
            Map<String, Object> dailyStudyLogCondition = new HashMap<>();
            dailyStudyLogCondition.put("rows", 5);
            dailyStudyLogCondition.put("order", "updatedDate");
            List<DailyStudyLog> dailyStudyLogs = dailyStudyMapper.getDailyStudyLogsByUserNo(
                user.getUserNo(), dailyStudyLogCondition);
            dto.setDailyStudyLogs(dailyStudyLogs);

            // 5. 사용자의 일일 도전과제 정보 조회
      /*
       고려할 사항
        1. 오늘 날짜와 컨텐츠타입 전달
       */
            Map<String, Object> dailyQuestUserCondition = new HashMap<>();
            dailyQuestUserCondition.put("completedDate", new Date());
            dailyQuestUserCondition.put("contentType", "STUDY");
            List<DailyQuestUser> dailyQuestUsers = dailyQuestUserMapper.getDailyQuestUsersByUserNo(
                user.getUserNo(), dailyQuestUserCondition);
            dto.setDailyQuestUsers(dailyQuestUsers);
        }

        return dto;
    }

    /**
     * 사용자의 수준과 진도에 맞는 추천 학습자료(DailyStudy) 목록을 조회한다. 최대 recommendStudyCount(기본 5개)까지 추천하며, 필요한 경우
     * GPT 기반으로 학습자료를 생성한다.
     * <p>
     * [전체 처리 단계]
     * <p>
     * 1단계: 사용자 진도(CURRENT_SCHOOL, CURRENT_GRADE)에 해당하는 원본 학습자료를 기준으로 학습자료 + 학습이력과 함께 아우터 조인하여 학습자료
     * DTO 목록을 조회한다. - 조건: 사용자의 수준(STUDY_DIFFICULTY)에 맞는 학습자료 - 조건: 학습이력이 없거나, 학습중(IN_PROGRESS) 상태인
     * 학습자료 - 정렬: 학습중이면 UPDATED_DATE 최신순, 그 외에는 자료순서(SEQUENCE) 순
     * <p>
     * 2단계: 추천 결과가 5개 미만일 경우 → 다음 학년의 학습자료로 부족한 수만큼 추가 조회한다. - 다음 학년이 없을 경우, "진도 종료" 상태로 간주 - 선택적으로
     * 사용자의 currentSchool/currentGrade를 업데이트할 수 있음
     * <p>
     * 3단계: 조회된 DTO 중 GPT 재구성된 학습자료가 없는 경우 → GPT 호출로 학습자료(DailyStudy)를 생성하고 DTO에 설정한다.
     * <p>
     * 4단계: 최종 결과 리스트에서 학습자료(DailyStudy)만 추출하여 반환한다.
     *
     * @param user                user 현재 사용자 정보
     * @param recommendStudyCount recommendStudyCount 반환할 학습자료 수
     * @return 학습자료 목록 (DailyStudy)
     */
    public List<UserStudyRecommendationDto> getUserDailyStudies(User user, int recommendStudyCount) {
        // 1단계 : 현재 사용자 수준/진도 기준으로 학습원본데이터_학습자료_학습이력DTO 목록 조회
      /*
        조회 조건
        1. 사용자 진도(CURRENT_SCHOOL, CURRENT_GRADE)에 해당하는 학습원본데이터 기준으로 학습자료,학습이력을 아우터 조인
        2. 학습자료가 있다면, 사용자 수준(STUDY_DIFFICULTY)에 맞는 학습자료만 포함
        3. 사용자의 학습 이력이 없거나,학습이력이 있어도 학습완료되지 않은 학습자료만 포함
        4. 사용자가 학습중이고, UPDATED_DATE가 최신인 학습자료를 우선 조회
        5. 그 후 학습원본데이터의 자료순서대로 조회 (즉, 사용자 진도의 학습원본데이터 중 사용자가 학습완료하지 않은 원본데이터를 조회)
        6. 최대 5개 조회
       */
        Map<String, Object> dailyStudyCondition = new HashMap<>();
        dailyStudyCondition.put("rows", recommendStudyCount);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("studyDifficulty", user.getStudyDifficulty());
        userInfo.put("currentSchool", user.getCurrentSchool());
        userInfo.put("currentGrade", user.getCurrentGrade());

        // 사용자 조건에 맞는 학습원본데이터_학습자료_학습이력DTO 5개 조회
        List<UserStudyRecommendationDto> userStudyRecommendationDtos = dailyStudyMapper.getDailyStudiesByUser(
            user.getUserNo(),
            userInfo,
            dailyStudyCondition
        );

        // 생성되는 DTO 확인 로그
        userStudyRecommendationDtos.forEach(dto -> log.info(dto.toString()));

        // 2단계 : 만약 학습원본데이터_학습자료_학습이력DTO가 5개 미만일 경우, 다음 학년에서 추가조회하여 채워넣기
        if (userStudyRecommendationDtos.size() < 5) {

            // 현재 사용자 학년의 다음 학년 계산 (다음 학년이 없으면 null반환)
            Pair<String, Integer> nextGrade = SchoolGradeUtils.getNextSchoolGrade(
                user.getCurrentSchool(), user.getCurrentGrade());

            // 만약 현재 학년에서 조회된 학습원본데이터_학습자료_학습이력DTO가 0개라면,
            // 사용자의 currentSchool/currenrGrade를 다음 학년으로 변경
            // ** 사용자 진도 변경하는 매퍼 호출 코드 추가 *************

            // 다음 학년이 null이 아닐때만 추가 조회
            if (nextGrade != null) {
                log.info("다음학년 추가 조회");
                // 더 조회해야할 학습자료 개수 조건 갱신
                int remainingRowCount = recommendStudyCount - userStudyRecommendationDtos.size();
                dailyStudyCondition.put("rows", remainingRowCount);

                // 다음 학년으로 조건 갱신
                userInfo.put("currentSchool", nextGrade.getLeft());
                userInfo.put("currentGrade", nextGrade.getRight());

                // 변경된 조건으로 학습원본데이터_학습자료_학습이력DTO 추가 조회
                List<UserStudyRecommendationDto> addUserStudyRecommendationDtos = dailyStudyMapper.getDailyStudiesByUser(
                    user.getUserNo(),
                    userInfo,
                    dailyStudyCondition
                );

                // 기존의 학습원본데이터_학습자료_학습이력DTO 리스트에 추가조회한 DTO 추가
                userStudyRecommendationDtos.addAll(addUserStudyRecommendationDtos);

                // 다음학년을 조회했음에도 채우지못했으면, 5개가 채워질때까지 학년을 변경하도록 설정
            } else {
                // 다음학년이 null이면 화면에 이를 식별하는 값을 전달하고,
                // 화면에서 마지막 학년 학습자료라는 표시되고 다른 수준 선택하는 등의 로직 처리
            }
        }

        // 3단계 : 조회한 학습원본데이터_학습자료_학습이력DTO 리스트에서 학습자료가 NULL값인 원본데이터에 대해 학습자료 생성하기
        for (UserStudyRecommendationDto rec : userStudyRecommendationDtos) {
            if (rec.getDailyStudyNo() == null) {
                // 해당 학습원본데이터와 사용자수준에 맞는 학습자료 생성하기
                DailyStudy newDailyStudy = createDailyStudy(rec.getDailyStudyMaterialNo(),
                    user.getStudyDifficulty());
                rec.setDailyStudy(newDailyStudy);
            }
        }

        // 4단게 : 최종 학습원본데이터_학습자료_학습이력DTO 를 컨트롤러에 전달

        return userStudyRecommendationDtos;
    }

    /**
     * 학습원본데이터를 기반으로 AI 재구성을 통해 학습자료를 DB에 저장하고, 반환하는 메소드
     * 1. 학습 원본자료와 사용자 수준의 프롬프트 텍스트 조회
     * 2. 프롬프트를 생성하고, 프롬프트를 Gemini에게 전달해 응답 반환
     * 3. 응답을 파싱해서 DB에 엔티티 insert
     * @param dailyStudyMaterialNo
     * @param studyDifficultyNo
     * @return
     */
    public DailyStudy createDailyStudy(int dailyStudyMaterialNo, int studyDifficultyNo) {
        // dailyStudyMaterialNo 로 학습자료 원본데이터 조회
         DailyStudyMaterial dailyStudyMaterial = dailyStudyMaterialMapper.getStudyMaterialByNo(dailyStudyMaterialNo);

         // studyDifficulty로 사용자 수준의 프롬프트 텍스트 조회
        StudyDifficulty studyDifficulty = studyDifficultyMapper.getDifficultyByNo(studyDifficultyNo);

         // 학습자료 원본데이터와 사용자의 학습 수준으로 프롬프트 생성
        String prompt = GeminiStudyPromptBuilder.buildPrompt(dailyStudyMaterial.getContent(),
            studyDifficulty.getPromptText());

        // Gemini에게 학습자료 원본 텍스트 전달해서, 응답 데이터 반환
        String content = geminiClient.getGeminiResponse(prompt);

        // 응답데이터에서 JSON만 추출
        String contentJsonOnly = content.substring(content.indexOf("{"), content.lastIndexOf("}") + 1);

        DailyStudy dailyStudy = null;
        // try문 범위 고민
        try {
            // Json응답데이터를 객체로 매핑
            GeminiStudyResponse geminiStudyResponse = objectMapper.readValue(contentJsonOnly, GeminiStudyResponse.class);

            // 학습자료, 학습자료카드, 학습퀴즈, 학습 서술형퀴즈를 DB에 저장하는 메소드 호출
            dailyStudy = insertDailyStudyWithOtherComponents(geminiStudyResponse, dailyStudyMaterialNo, studyDifficultyNo);
        } catch (JsonProcessingException e){
            log.error( e.getMessage());
            // 오류처리 추가
        }

        return dailyStudy;
    }

    /**
     * 전달받은 Gemini 응답 객체를 학습자료, 학습자료카드, 학습퀴즈, 학습 서술형퀴즈를 DB에 저장하는 메소드
     * @param response Gemini 응답 객체
     * @param dailyStudyMaterialNo 원본데이터 식별자
     * @param studyDifficultyNo 학습수준 식별자
     * @return DB에 저장된 학습자료 (DailyStudy)
     * @throws JsonProcessingException 호출하는 부분에서 try-catch 처리
     */
    @Transactional
    public DailyStudy insertDailyStudyWithOtherComponents(GeminiStudyResponse response, int dailyStudyMaterialNo, int studyDifficultyNo)
        throws JsonProcessingException {
        // 학습자료(DailyStudy) DB에 추가
        DailyStudy study = insertStudy(response, dailyStudyMaterialNo, studyDifficultyNo);
        // 학습자료카드(DailyStudyCard) DB에 추가
        insertCards(response.getCards(), study.getDailyStudyNo());
        // 학습자료퀴즈(DailyStudyQuiz) DB에 추가
        insertQuizzes(response.getQuizzes(), study.getDailyStudyNo());
        // 학습자료서술형쿼즈(DailyStudyEssayQuiz) DB에 추가
        insertEssayQuiz(response.getEssay(), study.getDailyStudyNo());

        return study;
    }

    /**
     * 학습자료(DailyStudy) DB에 추가하는 메소드 
     */
    private DailyStudy insertStudy(GeminiStudyResponse response, int dailyStudyMaterialNo, int studyDifficultyNo) {
        // 학습자료 테이블에 데이터 추가
        DailyStudy dailyStudy = new DailyStudy();
        dailyStudy.setStudyDifficulty(studyDifficultyNo);
        dailyStudy.setTitle(response.getMainTitle());
        dailyStudy.setDailyStudyMaterialNo(dailyStudyMaterialNo);
        dailyStudy.setCardCount(response.getCards().size());

        // 매퍼 호출
        dailyStudyMapper.insertDailyStudy(dailyStudy);

        return dailyStudy;
    }
    
    /**
     * 학습자료카드(DailyStudyCard) DB에 추가하는 메소드
     */
    private void insertCards(List<Card> cards, int dailyStudyNo) throws JsonProcessingException {
        // 학습 카드 테이블에 데이터 추가
        for (int i = 0; i < cards.size(); i++){
            Card card = cards.get(i);
            DailyStudyCard dailyStudyCard = new DailyStudyCard();
            dailyStudyCard.setTitle(card.getTitle());
            dailyStudyCard.setContent(objectMapper.writeValueAsString(card.getBody()));
            dailyStudyCard.setCardIndex(i + 1);
            dailyStudyCard.setDailyStudyNo(dailyStudyNo);

            // 매퍼 호출
            dailyStudyCardMapper.insertDailyStudyCard(dailyStudyCard);
        }
    }

    /**
     * 학습자료퀴즈(DailyStudyQuiz) DB에 추가하는 메소드
     */
    private void insertQuizzes(List<Quiz> quizzes, int dailyStudyNo) {
        // 퀴즈 테이블에 데이터 추가
        for (Quiz quiz : quizzes){
            DailyStudyQuiz dailyStudyQuiz = new DailyStudyQuiz();
            dailyStudyQuiz.setQuestion(quiz.getQuestion());
            dailyStudyQuiz.setOption1(quiz.getOptions().get(0));
            dailyStudyQuiz.setOption2(quiz.getOptions().get(1));
            dailyStudyQuiz.setOption3(quiz.getOptions().get(2));
            dailyStudyQuiz.setOption4(quiz.getOptions().get(3));
            dailyStudyQuiz.setSuccessAnswer(quiz.getAnswer());
            dailyStudyQuiz.setDailyStudyNo(dailyStudyNo);

            // 매퍼 호출
            dailyStudyQuizMapper.insertDailyStudyQuiz(dailyStudyQuiz);
        }
    }
    
    /**
     * 학습자료서술형쿼즈(DailyStudyEssayQuiz) DB에 추가하는 메소드
     */
    private void insertEssayQuiz(EssayQuiz essayQuiz, int dailyStudyNo) {
        // 생성형 퀴즈 테이블에 데이터 추가
        DailyStudyEssayQuiz dailyStudyEssayQuiz = new DailyStudyEssayQuiz();
        dailyStudyEssayQuiz.setQuestion(essayQuiz.getQuestion());
        dailyStudyEssayQuiz.setDailyStudyNo(dailyStudyNo);
        // 채점기준도 추가
        // 채점기준도 추가
        // 채점기준도 추가

        // 매퍼호출
        dailyStudyEssayQuizMapper.insertdailyStudyEssayQuiz(dailyStudyEssayQuiz);
    }

    /**
    * 요청으로 받은 에듀넷 URL 리스트를 큐에 넣고 각각의 상태를 DB에 저장
    * 파이썬 서버를 호출해 에듀넷 url에서 hwp 추출 후 텍스트 데이터를 반환받으면, 그 텍스트를 DB에 저장
    * @param request
    * @return
    */
    public ParseMaterialResponse createMaterial(ParseMaterialRequest request) {
        ParseMaterialResponse parseMaterialResponse = new ParseMaterialResponse();

        List<String> allUrls = request.getUrls();

        // 요청으로 받은 url 목록 중 이미 DB에 존재하는 url만 조회
        List<String> existingUrls = materialParseJobMapper.getExistUrls(allUrls);
        // API 반환 데이터에 skippedUrls 설정 (이미 처리된 경로라 스킵)
        parseMaterialResponse.setSkippedUrls(existingUrls);

        // db에 이미 있지 않은 신규 url만 필터링
        List<String> newUrls = allUrls.stream()
            .filter(url -> !existingUrls.contains(url))
            .toList();
        // API 반환 데이터에 enqueuedUrls 설정 (큐에 담긴후 백그라운드 작업 실행 될 url들)
        parseMaterialResponse.setEnqueuedUrls(newUrls);

        for (String fileUrl : newUrls) {

            MaterialParseJob materialParseJob = new MaterialParseJob();
            materialParseJob.setUrl(fileUrl);

            // 각 에듀넷 링크를 PARSE_JOB_STATUS 테이블에 저장
            materialParseJobMapper.insertParseJob(materialParseJob);

            // Redis에 넣을 JSON 형태의 메시지 생성 (jobId + url 포함)
            String jobPayload = String.format("{\"jobNo\":%d,\"url\":\"%s\"}",
                materialParseJob.getMaterialParseJobNo(),
                fileUrl);

            // 레디스에 URL 하나씩 푸시
            ListOperations<String, String> listOperations = stringRedisTemplate.opsForList();
            listOperations.rightPush("parse:queue", jobPayload);
        }

        return parseMaterialResponse;
    }

    /**
     * parseMaterialJobs 테이블에서 목록을 조회해서 반환
     */
    public List<MaterialParseJob> getMaterialParseJobs() {
        int rows = 10;

        List<MaterialParseJob> jobs = materialParseJobMapper.getParseJobsWithMaterial(rows);

        return jobs;
    }

}
