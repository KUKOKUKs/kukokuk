package com.kukokuk.domain.study.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.ai.GeminiClient;
import com.kukokuk.ai.GeminiStudyPromptBuilder;
import com.kukokuk.ai.GeminiStudyResponse;
import com.kukokuk.ai.GeminiStudyResponse.Card;
import com.kukokuk.ai.GeminiStudyResponse.EssayQuiz;
import com.kukokuk.ai.GeminiStudyResponse.Quiz;
import com.kukokuk.common.exception.AppException;
import com.kukokuk.common.store.RedisJobStatusStore;
import com.kukokuk.common.util.DailyQuestEnum;
import com.kukokuk.domain.quest.mapper.DailyQuestMapper;
import com.kukokuk.domain.quest.mapper.DailyQuestUserMapper;
import com.kukokuk.domain.quest.vo.DailyQuest;
import com.kukokuk.domain.quest.vo.DailyQuestUser;
import com.kukokuk.domain.quiz.dto.QuizWithLogDto;
import com.kukokuk.domain.study.dto.DailyQuestDto;
import com.kukokuk.domain.study.dto.DailyStudyJobPayload;
import com.kukokuk.domain.study.dto.DailyStudySummaryResponse;
import com.kukokuk.domain.study.dto.MainStudyViewDto;
import com.kukokuk.domain.study.dto.ParseMaterialRequest;
import com.kukokuk.domain.study.dto.StudyCompleteViewDto;
import com.kukokuk.domain.study.dto.StudyEssayViewDto;
import com.kukokuk.domain.study.dto.StudyProgressViewDto;
import com.kukokuk.domain.study.dto.UserStudyRecommendationDto;
import com.kukokuk.domain.study.mapper.DailyStudyCardMapper;
import com.kukokuk.domain.study.mapper.DailyStudyEssayQuizMapper;
import com.kukokuk.domain.study.mapper.DailyStudyLogMapper;
import com.kukokuk.domain.study.mapper.DailyStudyMapper;
import com.kukokuk.domain.study.mapper.DailyStudyMaterialMapper;
import com.kukokuk.domain.study.mapper.DailyStudyQuizLogMapper;
import com.kukokuk.domain.study.mapper.DailyStudyQuizMapper;
import com.kukokuk.domain.study.mapper.MaterialParseJobMapper;
import com.kukokuk.domain.study.mapper.StudyDifficultyMapper;
import com.kukokuk.domain.study.vo.DailyStudy;
import com.kukokuk.domain.study.vo.DailyStudyCard;
import com.kukokuk.domain.study.vo.DailyStudyEssayQuiz;
import com.kukokuk.domain.study.vo.DailyStudyEssayQuizLog;
import com.kukokuk.domain.study.vo.DailyStudyLog;
import com.kukokuk.domain.study.vo.DailyStudyMaterial;
import com.kukokuk.domain.study.vo.DailyStudyQuiz;
import com.kukokuk.domain.study.vo.DailyStudyQuizLog;
import com.kukokuk.domain.study.vo.MaterialParseJob;
import com.kukokuk.domain.study.vo.StudyDifficulty;
import com.kukokuk.domain.user.vo.User;
import com.kukokuk.domain.study.dto.EssayQuizLogRequest;
import com.kukokuk.domain.study.dto.StudyQuizLogRequest;
import com.kukokuk.domain.study.dto.UpdateStudyLogRequest;
import com.kukokuk.domain.study.dto.DailyStudyLogResponse;
import com.kukokuk.domain.study.dto.GeminiEssayResponse;
import com.kukokuk.domain.study.dto.ParseMaterialResponse;
import com.kukokuk.integration.redis.WorkerMaterialCallbackRequest;
import com.kukokuk.security.SecurityUser;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyService {

    private final  DailyStudyMapper dailyStudyMapper;
    private final DailyStudyLogMapper dailyStudyLogMapper;
    private final DailyQuestMapper dailyQuestMapper;
    private final DailyQuestUserMapper dailyQuestUserMapper;
    private final DailyStudyMaterialMapper dailyStudyMaterialMapper;
    private final StudyDifficultyMapper studyDifficultyMapper;
    private final DailyStudyCardMapper dailyStudyCardMapper;
    private final DailyStudyQuizMapper dailyStudyQuizMapper;
    private final DailyStudyQuizLogMapper dailyStudyQuizLogMapper;
    private final DailyStudyEssayQuizMapper dailyStudyEssayQuizMapper;
    private final MaterialParseJobMapper materialParseJobMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final GeminiClient geminiClient;

    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;

    private final RedisJobStatusStore<DailyStudySummaryResponse> studyJobStatusStore;

    /**
      메인 화면에 필요한 데이터를 담은 MainStudyViewDto를 반환한다
      <MainStudyViewDto 에 포함되는 데이터>
        1. 사용자의 이전 학습 이력 목록
        2. 학습탭의 일일 도전과제 목록 + 사용자의 일일 도전과제 수행 정보 (아이템 획득 여부)
     */
    @Transactional(readOnly = true) // 읽기 전용: 불필요한 트랜잭션 오버헤드 줄이기
    public MainStudyViewDto getMainStudyView(SecurityUser securityUser) {
        MainStudyViewDto dto = new MainStudyViewDto();

        // 1. 학습탭의 일일 도전과제 정보 조회 (로그인 여부와 무관)
        List<DailyQuest> dailyQuests = dailyQuestMapper.getDailyQuestByContentType("STUDY");
        // 2. 조회한 일일도전과제 목록을 DailyQuestDto로 변환 (사용자의 획득여부를 함께 표시하기 위한 DTO)
        // → 로그인되지 않은 경우 dailyQuestUser는 null, isSuccessed는 false 상태로 유지됨
        List<DailyQuestDto> dailyQuestDtos  = dailyQuests.stream()
            .map( quest -> modelMapper.map(quest, DailyQuestDto.class))
            .toList();

        //  3. 로그인된 사용자인 경우에만 사용자별 학습 이력 및 도전과제 수행 여부를 함께 조회
        //  (인증되지 않은 사용자는 미리 설정한 일일학습 자료 제공 예정)
        if (securityUser != null) { // 수정필요

            User user = securityUser.getUser();

            // 3-1. 사용자의 이전 학습이력 목록 5개 조회
            // → updatedDate 기준 정렬, 최대 5개
            Map<String, Object> dailyStudyLogCondition = new HashMap<>();
            dailyStudyLogCondition.put("rows", 5);
            dailyStudyLogCondition.put("order", "updatedDate");
            List<DailyStudyLog> dailyStudyLogs = dailyStudyLogMapper.getStudyLogsWithStudyByUserNo(
                user.getUserNo(), dailyStudyLogCondition);
            dto.setDailyStudyLogs(dailyStudyLogs);

            // 3-2. 사용자의 일일 도전과제 정보 조회
            // → 오늘 날짜 + STUDY 타입 기준
            Map<String, Object> dailyQuestUserCondition = new HashMap<>();
            dailyQuestUserCondition.put("completedDate", new Date());
            dailyQuestUserCondition.put("contentType", "STUDY");
            List<DailyQuestUser> dailyQuestUsers = dailyQuestUserMapper.getDailyQuestUsersByUserNo(
                user.getUserNo(), dailyQuestUserCondition);

            // 3-3. 조회한 사용자의 도전과제 수행 정보를 Map형태로 저장
            // key는 dailyQusetNo, value는 DailyQuestUser 객체 자체
            // dailyQusetNo를 앞서 조회한 dailyQuest와 비교하기 위함
            Map<Integer, DailyQuestUser> userQuestMap = dailyQuestUsers.stream()
                .collect(Collectors.toMap(
                     dailyQuestUser -> dailyQuestUser.getDailyQuestNo(),
                    Function.identity()  // 입력값(dailyQuestUser)을 그대로 사용
                ));

            // 3-4. 도전과제 목록을 순회하며 사용자의 도전과제 수행 정보와 매칭
            for (DailyQuestDto questDto : dailyQuestDtos){
                // 그 dailyQuest의 no를 가진 DailyQuestUser를 matchedUser에 저장
                DailyQuestUser matchedUser = userQuestMap.get(questDto.getDailyQuestNo());

                // 해당 도전과제를 수행한 사용자의 이력이 있을 경우
                if (matchedUser != null) {
                    // View로 넘길 dailyQuestDto의 dailyQuestUser필드 업데이트
                    questDto.setDailyQuestUser(matchedUser);
                    // View로 넘길 dailyQuestDto의 isSuccessed
                    questDto.setSuccessed(true);
                }
            }
        }

        // 4. 도전과제 - 사용자 도전과제 이력 DTO를 view에 넘길 dto에 저장
        dto.setDailyQuestDtos(dailyQuestDtos);

        return dto;
    }

    /**
     * 학습원본데이터와 학습수준에 맞는 학습자료를 조회
     * @param dailyStudyMaterialNo
     * @param studyDifficultyNo
     * @return
     */
    public UserStudyRecommendationDto getDailyStudyByMaterial(int dailyStudyMaterialNo, int studyDifficultyNo) {
        return dailyStudyMapper.getDailyStudyByMaterialNoAndDifficulty(dailyStudyMaterialNo, studyDifficultyNo);
    }


    public void generateStudy(DailyStudyJobPayload payload) {
        try {
            // 멱등 체크 - 이미 학습자료가 DB에 존재하면 새로 만들지 않고 DONE 처리
            UserStudyRecommendationDto existDto = getDailyStudyByMaterial(payload.getDailyStudyMaterialNo(), payload.getStudyDifficultyNo());

            // 이미 학습자료가 DB에 존재하는 경우, 작업상태를 DONE으로 업데이트 및 데이터 추가
            if (existDto !=null && existDto.getDailyStudy() != null) {
                studyJobStatusStore.update(payload.getJobId(), status -> {
                    status.setStatus("DONE");
                    status.setProgress(100);
                    status.setResult(mapToDailyStudySummaryResponse(existDto));
                    status.setMessage("이미 생성된 학습자료입니다");
                });
                return;
            }

            // AI 호출 작업 상태로 업데이트
            studyJobStatusStore.update(payload.getJobId(), status -> {
                status.setProgress(50);
                status.setMessage("맞춤 학습 자료 생성 중...");
            });

            // AI 호출 및 생성된 학습자료 DB 저장
            DailyStudy dailyStudy = createDailyStudyByAi(payload.getDailyStudyMaterialNo(),
                payload.getStudyDifficultyNo());

            // 생성된 학습자료를 기존의 dto에 업데이트
            existDto.setDailyStudyNo(dailyStudy.getDailyStudyNo());
            existDto.setDailyStudy(dailyStudy);

            // AI 호출 및 DB 저장 완료 상태로 업데이트
            studyJobStatusStore.update(payload.getJobId(), status -> {
                status.setStatus("DONE");
                status.setProgress(100);
                status.setResult(mapToDailyStudySummaryResponse(existDto));
                status.setMessage("학습 자료 생성 완료");
            });
        } catch (Exception e) {
            log.error("학습자료 생성 중 에러 발생. payload={}, error={}", payload, e.getMessage(), e);

            // 작업상태 실패로 업데이트
            studyJobStatusStore.update(payload.getJobId(), status -> {
                status.setStatus("FAILED");
                status.setProgress(100);
                status.setMessage("맞춤 학습 자료 생성에 실패하였습니다.\n다시 시도해 주세요.: " + e.getMessage());
            });
        }
    }



    /**
     * UserStudyRecommendationDto 리스트를 DailyStudySummaryResponse 리스트로 변환한다.
     *
     * 변환 과정에서 다음과 같은 추가 처리를 수행한다:
     * - DailyStudyLog 정보를 기반으로 학습 상태(status)와 진행률(progressRate) 계산
     * - 서술형 퀴즈 로그 번호(dailyStudyEssayQuizLogNo)가 존재하면 essayQuizCompleted를 true로 설정
     * @param dtos
     * @return API 응답용 DailyStudySummaryResponse 리스트
     */
    public List<DailyStudySummaryResponse> mapToDailyStudySummaryResponse(List<UserStudyRecommendationDto> dtos) {
        return dtos.stream()
            .filter(dto -> dto.getDailyStudy() != null)
            .map(dto -> {
                DailyStudy study = dto.getDailyStudy();
                DailyStudyLog log = dto.getDailyStudyLog();
                DailyStudyMaterial material = dto.getDailyStudyMaterial();

                int totalCardCount = study.getCardCount();
                int studiedCardCount = (log != null && log.getStudiedCardCount() != null) ? log.getStudiedCardCount() : 0;
                int progressRate =
                    (totalCardCount == 0) ? 0 : (int) ((studiedCardCount * 100.0) / totalCardCount);

                String status = "NOT_STARTED";
                if (log != null) {
                    status = log.getStatus(); // "IN_PROGRESS", "COMPLETED" 중 하나라고 가정
                }

                // dailyStudyEssayQuizLogNo 가 null이 아니면 서술형퀴즈완료여부 true로 설정
                boolean essayQuizCompleted = dto.getDailyStudyEssayQuizLogNo() != null;

                return DailyStudySummaryResponse.builder()
                    .dailyStudyNo(study.getDailyStudyNo())
                    .title(study.getTitle())
                    .explanation((study.getExplanation()))
                    .cardCount(totalCardCount)
                    .status(status)
                    .studiedCardCount(studiedCardCount)
                    .progressRate(progressRate)
                    .school(material.getSchool())
                    .grade(material.getGrade())
                    .sequence(material.getSequence())
                    .essayQuizCompleted(essayQuizCompleted)
                    .build();
            })
            .toList();
    }

    /**
     * UserStudyRecommendationDto 를 DailyStudySummaryResponse 로 변환한다.
     * @param dto
     * @return
     */
    public DailyStudySummaryResponse mapToDailyStudySummaryResponse(UserStudyRecommendationDto dto) {

        DailyStudy study = dto.getDailyStudy();
        DailyStudyLog log = dto.getDailyStudyLog();
        DailyStudyMaterial material = dto.getDailyStudyMaterial();

        int totalCardCount = study.getCardCount();
        int studiedCardCount = (log != null && log.getStudiedCardCount() != null) ? log.getStudiedCardCount() : 0;
        int progressRate =
            (totalCardCount == 0) ? 0 : (int) ((studiedCardCount * 100.0) / totalCardCount);

        String status = "NOT_STARTED";
        if (log != null) {
            status = log.getStatus(); // "IN_PROGRESS", "COMPLETED" 중 하나라고 가정
        }

        // dailyStudyEssayQuizLogNo 가 null이 아니면 서술형퀴즈완료여부 true로 설정
        boolean essayQuizCompleted = dto.getDailyStudyEssayQuizLogNo() != null;

        // material의 필드가 null일 경우 대비
        String school = (material != null && material.getSchool() != null)
            ? material.getSchool()
            : null;
        Integer grade = (material != null && material.getGrade() != null)
            ? material.getGrade()
            : null;

        return DailyStudySummaryResponse.builder()
            .dailyStudyNo(study.getDailyStudyNo())
            .title(study.getTitle())
            .explanation((study.getExplanation()))
            .cardCount(totalCardCount)
            .status(status)
            .studiedCardCount(studiedCardCount)
            .progressRate(progressRate)
            .school(school)
            .grade(grade)
            .sequence(material.getSequence())
            .essayQuizCompleted(essayQuizCompleted)
            .build();
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
    public DailyStudy createDailyStudyByAi(int dailyStudyMaterialNo, int studyDifficultyNo) {
        log.info("createDailyStudy 학습자료 생성 메소드 호출 | dailyStudyMaterialNo : " + dailyStudyMaterialNo + ", studyDifficultyNo : " + studyDifficultyNo);
        // dailyStudyMaterialNo 로 학습자료 원본데이터 조회
         DailyStudyMaterial dailyStudyMaterial = dailyStudyMaterialMapper.getStudyMaterialByNo(dailyStudyMaterialNo);

         // studyDifficulty로 사용자 수준의 프롬프트 텍스트 조회
        StudyDifficulty studyDifficulty = studyDifficultyMapper.getDifficultyByNo(studyDifficultyNo);

         // 학습자료 원본데이터와 사용자의 학습 수준으로 프롬프트 생성
        String prompt = GeminiStudyPromptBuilder.buildDailyStudyPrompt(dailyStudyMaterial.getContent(),
            studyDifficulty.getPromptText());

        // Gemini에게 학습자료 원본 텍스트 전달해서, 응답 데이터 반환
        String content = geminiClient.getGeminiResponse(prompt);

        // 응답데이터에서 JSON만 추출
        String contentJsonOnly = content.substring(content.indexOf("{"), content.lastIndexOf("}") + 1);

        // try문 범위 고민
        try {
            // Json응답데이터를 객체로 매핑
            GeminiStudyResponse geminiStudyResponse = objectMapper.readValue(contentJsonOnly, GeminiStudyResponse.class);

            log.info("geminiStudyResponse : " + geminiStudyResponse.getMainExplanation());

            // 학습자료, 학습자료카드, 학습퀴즈, 학습 서술형퀴즈를 DB에 저장하는 메소드 호출
            DailyStudy dailyStudy = insertDailyStudyWithOtherComponents(geminiStudyResponse, dailyStudyMaterialNo, studyDifficultyNo);
            log.info("저장된 학습자료 : " + dailyStudy.toString());

            // 생성된 학습자료 반환 
            return dailyStudy;

        } catch (JsonProcessingException e){
            log.error( e.getMessage());
            // 오류처리 추가
            // 여기서 던지면, 이 메소드를 호출하는 generateStudy에서 jobStatus를 failed로 처리
            throw new AppException("학습자료 JSON 파싱 실패", e);
        }

    }

    /**
     * 전달받은 Gemini 응답 객체를 학습자료, 학습자료카드, 학습퀴즈, 학습 서술형퀴즈를 DB에 저장하는 메소드
     * @param response Gemini 응답 객체
     * @param dailyStudyMaterialNo 원본데이터 식별자
     * @param studyDifficultyNo 학습수준 식별자
     * @return DB에 저장된 학습자료 (DailyStudy)
     * @throws JsonProcessingException 호출하는 부분에서 try-catch 처리
     */
    @Transactional(rollbackFor = {JsonProcessingException.class})
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
        dailyStudy.setExplanation(response.getMainExplanation());
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
    @Transactional
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

            try {
                // Redis에 넣을 JSON 형태의 메시지 생성 (jobId + url 포함)
                String jobPayload = String.format("{\"jobNo\":%d,\"url\":\"%s\"}",
                    materialParseJob.getMaterialParseJobNo(),
                    fileUrl);

                // 레디스에 URL 하나씩 푸시
                ListOperations<String, String> listOperations = stringRedisTemplate.opsForList();
                listOperations.rightPush("parse:queue", jobPayload);
            } catch (Exception e) {
                log.error("Redis push 실패: jobNo={}, url={}, error={}",
                    materialParseJob.getMaterialParseJobNo(), fileUrl, e.getMessage());

                // 레디스 푸시실패시 해당 job을 Failed로 표시
                materialParseJobMapper.updateParseJobStatusToFailed(materialParseJob.getMaterialParseJobNo(),
                    e.getMessage());
            }
        }

        return parseMaterialResponse;
    }

    /**
     * parseMaterialJobs 테이블에서 목록을 조회해서 반환
     */
    public List<MaterialParseJob> getMaterialParseJobs() {
        int rows = 100;

        List<MaterialParseJob> jobs = materialParseJobMapper.getParseJobsWithMaterial(rows);

        return jobs;
    }

    /**
     * 학습 진행 화면에 필요한 데이터를 담은 StudyProgressViewDto 반환한다
     *       <StudyProgressViewDto 에 포함되는 데이터>
     *         1. dailyStudyNo에 해당하는 일일학습 정보
     *         2. 일일학습 카드 목록
     *         3. 사용자의 일일학습 이력
     *         4. 일일학습 퀴즈 목록
     *         5. 사용자의 일일학습 퀴즈 이력 목록
     */
    @Transactional(readOnly = true)
    public StudyProgressViewDto getStudyProgressView(int dailyStudyNo, int userNo) {
        StudyProgressViewDto dto = new StudyProgressViewDto();

        // 1. dailyStudyNo에 해당하는 일일학습 정보 조회
        DailyStudy dailyStudy = dailyStudyMapper.getDailyStudyByNo(dailyStudyNo);

        dto.setDailyStudy(dailyStudy);

        // 2. 해당 일일학습에 속한 일일학습 카드 목록 조회
        List<DailyStudyCard> studyCards = dailyStudyCardMapper.getCardsByDailyStudyNo(dailyStudyNo);

        dto.setCards(studyCards);

        // 3. 사용자의 일일학습 이력 조회
        DailyStudyLog studyLog = dailyStudyLogMapper.getStudyLogByUserNoAndDailyStudyNo(userNo, dailyStudyNo);

        dto.setLog(studyLog);

        // 4. 일일학습 퀴즈 목록 조회
        List<DailyStudyQuiz> studyQuizzes = dailyStudyQuizMapper.getStudyQuizzesByDailyStudyNo(dailyStudyNo);

        dto.setQuizzes(studyQuizzes);

        // 5. 사용자의 일일학습 퀴즈 이력 목록
        List<DailyStudyQuizLog> studyQuizLogs = dailyStudyQuizLogMapper.getStudyQuizLogsByUserNoAndDailyStudyNo(userNo, dailyStudyNo);

        dto.setQuizLogs(studyQuizLogs);

        // 사용자의 퀴즈 이력을 quizNo기준의 Map으로 변환
        Map<Integer, DailyStudyQuizLog> quizLogMap = studyQuizLogs.stream()
              .collect(Collectors.toMap(DailyStudyQuizLog::getDailyStudyQuizNo, Function.identity()));

        // 퀴즈와 사용자의 이력을 합친 DTO 생성
        List<QuizWithLogDto> quizWithLogDtos = studyQuizzes.stream()
                .map(quiz -> {
                    QuizWithLogDto quizWithLogDto = modelMapper.map(quiz, QuizWithLogDto.class);
                    quizWithLogDto.setDailyStudyQuizLog(quizLogMap.get(quiz.getDailyStudyQuizNo()));
                    return quizWithLogDto;
                })
                .toList();

        dto.setQuizWithLogDtos(quizWithLogDtos);
      
        return dto;
    }

    /**
     * 해당 사용자의 해당 학습자료에 대한 이력 생성
     * @param dailyStudyNo
     * @param userNo
     * @return
     */
    @Transactional
    public DailyStudyLog createDailyStudyLog(int dailyStudyNo, int userNo) {

        // 이미 존재하는 학습이력이 있는지 확인
        DailyStudyLog existLog =  dailyStudyLogMapper.getStudyLogByUserNoAndDailyStudyNo(userNo, dailyStudyNo);

        // 이미 존재하는 학습이력이면 에러 발생
        if (existLog != null) {
            throw new AppException("이미 해당 학습자료에대한 사용자의 이력이 존재합니다");
        }

        // 학습이력 생성
        DailyStudyLog log = new DailyStudyLog();
        log.setDailyStudyNo(dailyStudyNo);
        log.setUserNo(userNo);

        dailyStudyLogMapper.createStudyLog(log);

        // 학습이력 번호로 조회해서 반환
        return dailyStudyLogMapper.getStudyLogByNo(log.getDailyStudyLogNo());
    }

    /**
     * 해당 사용자의 해당 학습자료에 대한 이력 수정
     * @param dailyStudyLogNo
     * @param userNo
     * @return
     */
    @Transactional
    public DailyStudyLogResponse updateDailyStudyLog(int dailyStudyLogNo, UpdateStudyLogRequest updateStudyLogRequest, int userNo) {
        log.info("updateStudyLogRequest 서비스 실행");

        // 학습이력의 사용자와 현재사용자가 일치하지않으면 권한 에러 발생
        DailyStudyLog existedlog = dailyStudyLogMapper.getStudyLogByNo(dailyStudyLogNo);
        if(existedlog.getUserNo() != userNo){
            throw new AccessDeniedException("본인의 학습이력만 수정할 수 있습니다");
        }

        // 이미 학습완료 상태인 이력인지 여부
        boolean alreadyCompleted = "COMPLETED".equals(existedlog.getStatus());
        // 이번 요청에서 힌트를 획득했는지 여부
        boolean questCompleted = false;

        // 수정 전 학습이력이 '학습완료'상태가 아니고, 수정 후 학습이력이 '학습완료' 상태일 경우
        // 오늘 달성한 도전과제가 있는지 확인하고, 없으면 도전과제 달성이력을 추가한다
        if (!alreadyCompleted && "COMPLETED".equals(updateStudyLogRequest.getStatus())) {

            // 오늘 날짜의 도전과제 수행 이력이 존재하는지 확인
            DailyQuestUser existQuestUser = dailyQuestUserMapper.getTodayQuestUserByUserNoAndQuestNo(userNo, DailyQuestEnum.COMPLETED_DAILY_STUDY.getDailyQuestNo());

            // 오늘의 도전과제 수행 이력이 존재하지 않을 때만
            if (existQuestUser == null) {
                DailyQuestUser dailyQuestUser = new DailyQuestUser();
                dailyQuestUser.setDailyQuestNo(DailyQuestEnum.COMPLETED_DAILY_STUDY.getDailyQuestNo());
                dailyQuestUser.setUserNo(userNo);

                dailyQuestUserMapper.insertDailyQuestUser(dailyQuestUser);
                log.info("오늘의 도전과제를 달성했습니다");
            }

            questCompleted = true;
        }

        // 수정할 컬럼만 담은 VO객체로 수정 mapper 호출
        DailyStudyLog updateLog = modelMapper.map(updateStudyLogRequest, DailyStudyLog.class);
        updateLog.setDailyStudyLogNo(dailyStudyLogNo);

        // 만약 이미 학습완료 상태인데 STATUS를 수정하려는 경우 (복습 등), STATUS는 변경되지 않게 한다
        if (alreadyCompleted) {
            updateLog.setStatus(null);
        }

        dailyStudyLogMapper.updateStudyLog(updateLog);

        // 수정된 학습이력 조회
        DailyStudyLog updatedLog = dailyStudyLogMapper.getStudyLogByNo(dailyStudyLogNo);
        DailyStudyLogResponse response = modelMapper.map(updatedLog, DailyStudyLogResponse.class);
        response.setQuestCompleted(questCompleted);
        return response;
     }

    /**
     * 학습퀴즈이력 생성 
     * ㄴ 이미 존재하는 학습이력이 있으면, 수정 로직 호출
     * @param studyQuizLogRequest
     * @param userNo
     * @return
     */
    @Transactional
    public DailyStudyQuizLog createStudyQuizLog(StudyQuizLogRequest studyQuizLogRequest, int userNo) {
        log.info("createStudyQuizLog 서비스 실행");

        int studyQuizNo = studyQuizLogRequest.getDailyStudyQuizNo();
        int selectedChoice = studyQuizLogRequest.getSelectedChoice();

        // 사용자번호와 학습퀴즈번호로 이미 존재하는 퀴즈 이력이 있는지 확인
        DailyStudyQuizLog existLog
            = dailyStudyQuizLogMapper.getStudyQuizLogsByUserNoAndStudyQuizNo(userNo, studyQuizNo);

        // 만약 이미 존재하는 퀴즈면 에러 발생 - 프론트에서 수정호출하도록 처리
        if (existLog != null) {
            log.info("이미 학습퀴즈이력이 존재하므로 update로직 호출");
            return updateStudyQuizLog(existLog.getDailyStudyQuizLogNo(), studyQuizLogRequest, userNo);
        }

        // studyQuizNo로 해당 퀴즈 조회후, 사용자 선택보기와 비교해서 정답여부 설정
        DailyStudyQuiz quiz = dailyStudyQuizMapper.getStudyQuizByNo(studyQuizNo);

        String isSuccess = quiz.getSuccessAnswer() == selectedChoice ? "Y" : "N";

        // 학습퀴즈이력 생성
        DailyStudyQuizLog dailyStudyQuizLog = new DailyStudyQuizLog();
        dailyStudyQuizLog.setDailyStudyQuizNo(studyQuizNo);
        dailyStudyQuizLog.setUserNo(userNo);
        dailyStudyQuizLog.setSelectedChoice(studyQuizLogRequest.getSelectedChoice());
        dailyStudyQuizLog.setIsSuccess(isSuccess);

        dailyStudyQuizLogMapper.createStudyQuizLog(dailyStudyQuizLog);

        return dailyStudyQuizLogMapper.getStudyQuizLogsByNo(dailyStudyQuizLog.getDailyStudyQuizLogNo());
    }

    /**
     * 학습퀴즈이력 수정
     * @param studyQuizLogNo
     * @param studyQuizLogRequest
     * @return
     */
    @Transactional
    public DailyStudyQuizLog updateStudyQuizLog(int studyQuizLogNo, StudyQuizLogRequest studyQuizLogRequest, int userNo) {
        log.info("updateStudyQuizLog 서비스 실행");

        // 학습퀴즈이력의 사용자와 현재사용자가 일치하지않으면 권한 에러 발생
        DailyStudyQuizLog log = dailyStudyQuizLogMapper.getStudyQuizLogsByNo(studyQuizLogNo);
        if (log.getUserNo() != userNo) {
            throw new AccessDeniedException("본인의 학습퀴즈이력만 수정할 수 있습니다");
        }

        // studyQuizNo로 해당 퀴즈 조회후, 사용자 선택보기와 비교해서 정답여부 설정
        DailyStudyQuiz quiz = dailyStudyQuizMapper.getStudyQuizByNo(log.getDailyStudyQuizNo());

        int selectedChoice = studyQuizLogRequest.getSelectedChoice();
        String isSuccess = quiz.getSuccessAnswer() == selectedChoice ? "Y" : "N";

        // 조회했던 학습퀴즈이력에서 수정 필드만 변경후 수정 매퍼 호출
        log.setSelectedChoice(selectedChoice);
        log.setIsSuccess(isSuccess);
        dailyStudyQuizLogMapper.updateStudyQuizLog(log);

        return log;
    }

    /**
     * 학습수준 목록을 조회
     * @return
     */
    @Transactional(readOnly = true)
    public List<StudyDifficulty> getStudyDifficulties() {
        return studyDifficultyMapper.getDifficulties();
    }

    /**
     * 서술형 퀴즈 화면에 필요한 데이터 조회
     * @param dailyStudyNo
     * @param userNo
     */
    @Transactional(readOnly = true)
    public StudyEssayViewDto getStudyEssayView(int dailyStudyNo, Integer userNo) {
        log.info("getStudyEssayView 서비스 실행");

        StudyEssayViewDto dto = new StudyEssayViewDto();

        // 서술형 퀴즈 데이터 조회
        DailyStudyEssayQuiz essay = dailyStudyEssayQuizMapper.getEssayQuizByDailyStudyNo(dailyStudyNo);
        dto.setEssay(essay);

        // 사용자의 서술형 퀴즈 이력 조회
        if (userNo != null) {
            DailyStudyEssayQuizLog essayLog = dailyStudyEssayQuizMapper.getEssayQuizLogByQuizNoAndUserNo(essay.getDailyStudyEssayQuizNo(),userNo);
            dto.setEssayLog(essayLog);
        }

        return dto;
    }

    /**
     * 서술형 퀴즈 이력 생성
     * @param request
     * @param userNo
     * @return
     */
    @Transactional
    public DailyStudyEssayQuizLog createStudyEssayQuizLog(EssayQuizLogRequest request, int userNo) {
        log.info("createStudyEssayQuizLog 서비스 실행");

        // 이미 존재하는 사용자의 서술형 퀴즈 이력이 존재하는지 확인 후 , 존재하면 업데이트 메소드 호출
        DailyStudyEssayQuizLog existLog = dailyStudyEssayQuizMapper.getEssayQuizLogByQuizNoAndUserNo(request.getDailyStudyEssayQuizNo(), userNo);
        if (existLog != null) {
            return updateStudyEssayQuizLog(existLog.getDailyStudyEssayQuizLogNo(), request, userNo);
        }

        // DTO를 VO객체로 변환
        DailyStudyEssayQuizLog essayQuizLog = modelMapper.map(request, DailyStudyEssayQuizLog.class);
        essayQuizLog.setUserNo(userNo);

        // 서술형퀴즈 이력 생성 매퍼 호출
        dailyStudyEssayQuizMapper.insertStudyEssayQuizLog(essayQuizLog);

        // 생성된 이력을 조회해서 반환
        return dailyStudyEssayQuizMapper.getEssayQuizLogByNo(essayQuizLog.getDailyStudyEssayQuizLogNo());
    }

    /**
     * 서술형 퀴즈 이력 수정
     * @param dailyStudyEssayQuizLogNo
     * @param request
     * @param userNo
     * @return
     */
    @Transactional
    public DailyStudyEssayQuizLog updateStudyEssayQuizLog(int dailyStudyEssayQuizLogNo ,
        EssayQuizLogRequest request, int userNo) {
        log.info("updateStudyEssayQuizLog 서비스 실행");

        // 서술형퀴즈이력의 사용자와 현재사용자가 일치하지않으면 권한 에러 발생
        DailyStudyEssayQuizLog essayQuizLog = dailyStudyEssayQuizMapper.getEssayQuizLogByNo(dailyStudyEssayQuizLogNo);
        if (essayQuizLog.getUserNo() != userNo) {
            throw new AccessDeniedException("본인의 서술형퀴즈 이력만 수정할 수 있습니다");
        }

        // 조회했던 서술형퀴즈이력에서 수정 필드만 변경후 수정 매퍼 호출
        essayQuizLog.setUserAnswer(request.getUserAnswer());
        dailyStudyEssayQuizMapper.updateStudyEssayQuizLog(essayQuizLog);

        return essayQuizLog;
    }

    /**
     * 사용자의 학습 수준에 맞게 논술형퀴즈 AI 피드백을 생성 및 DB에 반영
     * @param essayQuizLog
     * @param studyDifficultyNo
     * @return
     */
    public GeminiEssayResponse generateAiFeedback(DailyStudyEssayQuizLog essayQuizLog,
        int studyDifficultyNo) {
        log.info("generateAiFeedback 서비스 메소드 실행");

        // 이력에 존재하는 서술형 퀴즈 번호로 서술형퀴즈 조회 (질문 컬럼 조회 목적)
        DailyStudyEssayQuiz essayQuiz = dailyStudyEssayQuizMapper.getEssayQuizByNo(
            essayQuizLog.getDailyStudyEssayQuizNo());

        // studyDifficulty로 사용자 수준의 프롬프트 텍스트 조회
        StudyDifficulty studyDifficulty = studyDifficultyMapper.getDifficultyByNo(
            studyDifficultyNo);

        // 서술형 질문과 사용자 답변, 사용자의 학습 수준으로 프롬프트 생성
        String prompt = GeminiStudyPromptBuilder.buildEssayQuizPrompt(
            essayQuiz.getQuestion(),
            essayQuizLog.getUserAnswer(),
            studyDifficulty.getPromptText()
        );

        // Gemini에게 학습자료 원본 텍스트 전달해서, 응답 데이터 반환
        String content = geminiClient.getGeminiResponse(prompt);

        // 응답데이터에서 JSON만 추출
        String contentJsonOnly = content.substring(content.indexOf("{"),
            content.lastIndexOf("}") + 1);

        // JSON 응답데이터를 essayQuizLog의 aiFeedback필드에 업데이트하는 메소드 호출
        // 트랜잭션 처리를 위해 DB 접근 작업 별도 분리
        updateAiFeedback(essayQuizLog, contentJsonOnly);

        // try문 범위 고민
        GeminiEssayResponse geminiEssayResponse = null;

        try {
            // Json응답데이터를 객체로 매핑
            geminiEssayResponse = objectMapper.readValue(contentJsonOnly,
                GeminiEssayResponse.class);

        } catch (JsonProcessingException e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage());
            throw new AppException("응답: " + contentJsonOnly);
        }

        return geminiEssayResponse;
    }

    /**
     * 논술형퀴즈에서 생성된 AI 피드백을 DB에 업데이트하는 메소드
     * @param essayQuizLog
     * @param contentJsonOnly
     */
    @Transactional
    public void updateAiFeedback(DailyStudyEssayQuizLog essayQuizLog, String contentJsonOnly) {
        log.info("updateAiFeedback 서비스 메소드 실행");
        
        essayQuizLog.setAiFeedback(contentJsonOnly);
        dailyStudyEssayQuizMapper.updateStudyEssayQuizLog(essayQuizLog);
    }

    /**
     * 학습 완료 화면에 필요한 데이터 조회
     * @param dailyStudyNo
     * @param userNo
     * @return
     */
    @Transactional(readOnly = true)
    public StudyCompleteViewDto getStudyCompleteView(int dailyStudyNo, int userNo) {
        log.info("getStudyCompleteView 서비스 메소드 실행");

        StudyCompleteViewDto dto = new StudyCompleteViewDto();

        DailyStudyLog studyLog = dailyStudyLogMapper.getStudyLogByUserNoAndDailyStudyNo(userNo, dailyStudyNo);
        dto.setLog(studyLog);

        return dto;
    }
}