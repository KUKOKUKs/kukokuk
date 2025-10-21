package com.kukokuk.domain.study.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.ai.GeminiClient;
import com.kukokuk.ai.GeminiStudyPromptBuilder;
import com.kukokuk.ai.GeminiStudyResponse;
import com.kukokuk.ai.GeminiStudyResponse.Card;
import com.kukokuk.ai.GeminiStudyResponse.EssayQuiz;
import com.kukokuk.ai.GeminiStudyResponse.Quiz;
import com.kukokuk.common.constant.ContentTypeEnum;
import com.kukokuk.common.constant.DailyQuestEnum;
import com.kukokuk.common.dto.JobStatusResponse;
import com.kukokuk.common.dto.Page;
import com.kukokuk.common.dto.Pagination;
import com.kukokuk.common.exception.AppException;
import com.kukokuk.common.service.ObjectStorageService;
import com.kukokuk.common.store.RedisJobStatusStore;
import com.kukokuk.domain.exp.dto.ExpProcessingDto;
import com.kukokuk.domain.exp.service.ExpProcessingService;
import com.kukokuk.domain.quiz.dto.QuizWithLogDto;
import com.kukokuk.domain.study.dto.AdminParseMaterialResponse;
import com.kukokuk.domain.study.dto.DailyStudyJobPayload;
import com.kukokuk.domain.study.dto.DailyStudyLogDetailResponse;
import com.kukokuk.domain.study.dto.DailyStudyLogResponse;
import com.kukokuk.domain.study.dto.DailyStudySummaryResponse;
import com.kukokuk.domain.study.dto.EssayQuizLogRequest;
import com.kukokuk.domain.study.dto.GeminiEssayResponse;
import com.kukokuk.domain.study.dto.StudyCompleteViewDto;
import com.kukokuk.domain.study.dto.StudyEssayViewDto;
import com.kukokuk.domain.study.dto.StudyMaterialJobPayload;
import com.kukokuk.domain.study.dto.StudyProgressViewDto;
import com.kukokuk.domain.study.dto.StudyQuizLogRequest;
import com.kukokuk.domain.study.dto.UpdateStudyLogRequest;
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
import com.kukokuk.integration.redis.WorkerMaterialCallbackRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Log4j2
public class StudyService {

    private final DailyStudyMapper dailyStudyMapper;
    private final DailyStudyLogMapper dailyStudyLogMapper;
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

    private final ExpProcessingService expProcessingService;
    private final ObjectStorageService objectStorageService;

    private final RedisJobStatusStore<DailyStudySummaryResponse> studyJobStatusStore;
    private final RedisJobStatusStore<AdminParseMaterialResponse> adminParseJobStatusStore;

    /**
     * // 메인 화면에 필요한 데이터를 담은 MainStudyViewDto를 반환한다 <MainStudyViewDto 에 포함되는 데이터> 1. 사용자의 이전 학습 이력 목록
     * // 2. 학습탭의 일일 도전과제 목록 + 사용자의 일일 도전과제 수행 정보 (아이템 획득 여부)
     *
     * 수정됨
     * 기능 수정으로 메서드 명도 수정
     * 
     * 사용자 번호로 학습 이력 목록 정보 조회
     * @param userNo 사용자 번호
     * @return 학습 이력 목록 정보
     */
    public List<DailyStudyLog> getDailyStudyLogs(int userNo, int rows) {
        log.info("getMainStudyView() 실행");
        Map<String, Object> dailyStudyLogCondition = new HashMap<>();
        dailyStudyLogCondition.put("rows", rows);
        dailyStudyLogCondition.put("order", "updatedDate");
        return dailyStudyLogMapper.getStudyLogsWithStudyByUserNo(userNo, dailyStudyLogCondition);
    }

    /**
     * 학습원본데이터와 학습수준에 맞는 학습자료를 조회
     *
     * @param dailyStudyMaterialNo
     * @param studyDifficultyNo
     * @return
     */
    public UserStudyRecommendationDto getDailyStudyByMaterial(int dailyStudyMaterialNo,
        int studyDifficultyNo) {
        return dailyStudyMapper.getDailyStudyByMaterialNoAndDifficulty(dailyStudyMaterialNo,
            studyDifficultyNo);
    }


    public void generateStudy(DailyStudyJobPayload payload) {
        try {
            // 멱등 체크 - 이미 학습자료가 DB에 존재하면 새로 만들지 않고 DONE 처리
            UserStudyRecommendationDto existDto = getDailyStudyByMaterial(
                payload.getDailyStudyMaterialNo(), payload.getStudyDifficultyNo());

            // 이미 학습자료가 DB에 존재하는 경우, 작업상태를 DONE으로 업데이트 및 데이터 추가
            if (existDto != null && existDto.getDailyStudy() != null) {
                studyJobStatusStore.update(payload.getJobId(), status -> {
                    status.setStatus("DONE");
                    status.setProgress(100);
                    status.setResult(mapToDailyStudySummaryResponse(existDto));
                    status.setMessage("학습 자료 생성이 완료되었습니다.");
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
                status.setMessage("학습 자료 생성이 완료되었습니다.");
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
     * <p>
     * 변환 과정에서 다음과 같은 추가 처리를 수행한다: - DailyStudyLog 정보를 기반으로 학습 상태(status)와 진행률(progressRate) 계산 -
     * 서술형 퀴즈 로그 번호(dailyStudyEssayQuizLogNo)가 존재하면 essayQuizCompleted를 true로 설정
     *
     * @param dtos
     * @return API 응답용 DailyStudySummaryResponse 리스트
     */
    public List<DailyStudySummaryResponse> mapToDailyStudySummaryResponse(
        List<UserStudyRecommendationDto> dtos) {
        return dtos.stream()
            .filter(dto -> dto.getDailyStudy() != null)
            .map(dto -> {
                DailyStudy study = dto.getDailyStudy();
                DailyStudyLog log = dto.getDailyStudyLog();
                DailyStudyMaterial material = dto.getDailyStudyMaterial();

                int totalCardCount = study.getCardCount();
                int studiedCardCount =
                    (log != null && log.getStudiedCardCount() != null) ? log.getStudiedCardCount()
                        : 0;
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
     *
     * @param dto
     * @return
     */
    public DailyStudySummaryResponse mapToDailyStudySummaryResponse(
        UserStudyRecommendationDto dto) {

        DailyStudy study = dto.getDailyStudy();
        DailyStudyLog log = dto.getDailyStudyLog();
        DailyStudyMaterial material = dto.getDailyStudyMaterial();

        int totalCardCount = study.getCardCount();
        int studiedCardCount =
            (log != null && log.getStudiedCardCount() != null) ? log.getStudiedCardCount() : 0;
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
     * 학습원본데이터를 기반으로 AI 재구성을 통해 학습자료를 DB에 저장하고, 반환하는 메소드 1. 학습 원본자료와 사용자 수준의 프롬프트 텍스트 조회 2. 프롬프트를
     * 생성하고, 프롬프트를 Gemini에게 전달해 응답 반환 3. 응답을 파싱해서 DB에 엔티티 insert
     *
     * @param dailyStudyMaterialNo
     * @param studyDifficultyNo
     * @return
     */
    public DailyStudy createDailyStudyByAi(int dailyStudyMaterialNo, int studyDifficultyNo) {
        log.info("createDailyStudy 학습자료 생성 메소드 호출 | dailyStudyMaterialNo : " + dailyStudyMaterialNo
            + ", studyDifficultyNo : " + studyDifficultyNo);
        // dailyStudyMaterialNo 로 학습자료 원본데이터 조회
        DailyStudyMaterial dailyStudyMaterial = dailyStudyMaterialMapper.getStudyMaterialByNo(
            dailyStudyMaterialNo);

        // studyDifficulty로 사용자 수준의 프롬프트 텍스트 조회
        StudyDifficulty studyDifficulty = studyDifficultyMapper.getDifficultyByNo(
            studyDifficultyNo);

        // 학습자료 원본데이터와 사용자의 학습 수준으로 프롬프트 생성
        String prompt = GeminiStudyPromptBuilder.buildDailyStudyPrompt(
            dailyStudyMaterial.getContent(),
            studyDifficulty.getPromptText());

        // Gemini에게 학습자료 원본 텍스트 전달해서, 응답 데이터 반환
        String content = geminiClient.getGeminiResponse(prompt);

        // 응답데이터에서 JSON만 추출
        String contentJsonOnly = content.substring(content.indexOf("{"),
            content.lastIndexOf("}") + 1);

        // try문 범위 고민
        try {
            // Json응답데이터를 객체로 매핑
            GeminiStudyResponse geminiStudyResponse = objectMapper.readValue(contentJsonOnly,
                GeminiStudyResponse.class);

            log.info("geminiStudyResponse : " + geminiStudyResponse.getMainExplanation());

            // 학습자료, 학습자료카드, 학습퀴즈, 학습 서술형퀴즈를 DB에 저장하는 메소드 호출
            DailyStudy dailyStudy = insertDailyStudyWithOtherComponents(geminiStudyResponse,
                dailyStudyMaterialNo, studyDifficultyNo);
            log.info("저장된 학습자료 : " + dailyStudy.toString());

            // 생성된 학습자료 반환 
            return dailyStudy;

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            // 오류처리 추가
            // 여기서 던지면, 이 메소드를 호출하는 generateStudy에서 jobStatus를 failed로 처리
            throw new AppException("학습자료 JSON 파싱 실패", e);
        }

    }

    /**
     * 전달받은 Gemini 응답 객체를 학습자료, 학습자료카드, 학습퀴즈, 학습 서술형퀴즈를 DB에 저장하는 메소드
     *
     * @param response             Gemini 응답 객체
     * @param dailyStudyMaterialNo 원본데이터 식별자
     * @param studyDifficultyNo    학습수준 식별자
     * @return DB에 저장된 학습자료 (DailyStudy)
     * @throws JsonProcessingException 호출하는 부분에서 try-catch 처리
     */
    @Transactional(rollbackFor = {JsonProcessingException.class})
    public DailyStudy insertDailyStudyWithOtherComponents(GeminiStudyResponse response,
        int dailyStudyMaterialNo, int studyDifficultyNo)
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
    private DailyStudy insertStudy(GeminiStudyResponse response, int dailyStudyMaterialNo,
        int studyDifficultyNo) {
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
        for (int i = 0; i < cards.size(); i++) {
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
        for (Quiz quiz : quizzes) {
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
     * parseMaterialJobs 테이블에서 목록을 조회해서 반환
     */
    public List<MaterialParseJob> getMaterialParseJobs() {
        int rows = 100;

        List<MaterialParseJob> jobs = materialParseJobMapper.getParseJobsWithMaterial(rows);

        return jobs;
    }

    /**
     * 학습 진행 화면에 필요한 데이터를 담은 StudyProgressViewDto 반환한다 <StudyProgressViewDto 에 포함되는 데이터> 1.
     * dailyStudyNo에 해당하는 일일학습 정보 2. 일일학습 카드 목록 3. 사용자의 일일학습 이력 4. 일일학습 퀴즈 목록 5. 사용자의 일일학습 퀴즈 이력 목록
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
        DailyStudyLog studyLog = dailyStudyLogMapper.getStudyLogByUserNoAndDailyStudyNo(userNo,
            dailyStudyNo);

        dto.setLog(studyLog);

        // 4. 일일학습 퀴즈 목록 조회
        List<DailyStudyQuiz> studyQuizzes = dailyStudyQuizMapper.getStudyQuizzesByDailyStudyNo(
            dailyStudyNo);

        dto.setQuizzes(studyQuizzes);

        // 5. 사용자의 일일학습 퀴즈 이력 목록
        List<DailyStudyQuizLog> studyQuizLogs = dailyStudyQuizLogMapper.getStudyQuizLogsByUserNoAndDailyStudyNo(
            userNo, dailyStudyNo);

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
     *
     * @param dailyStudyNo
     * @param userNo
     * @return
     */
    @Transactional
    public DailyStudyLog createDailyStudyLog(int dailyStudyNo, int userNo) {

        // 이미 존재하는 학습이력이 있는지 확인
        DailyStudyLog existLog = dailyStudyLogMapper.getStudyLogByUserNoAndDailyStudyNo(userNo,
            dailyStudyNo);

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
     *
     * @param dailyStudyLogNo
     * @param userNo
     * @return
     */
    @Transactional
    public DailyStudyLogResponse updateDailyStudyLog(int dailyStudyLogNo,
        UpdateStudyLogRequest updateStudyLogRequest, int userNo) {
        log.info("updateStudyLogRequest 서비스 실행");

        // 학습이력의 사용자와 현재사용자가 일치하지않으면 권한 에러 발생
        DailyStudyLog existedlog = dailyStudyLogMapper.getStudyLogByNo(dailyStudyLogNo);
        if (existedlog.getUserNo() != userNo) {
            throw new AccessDeniedException("본인의 학습이력만 수정할 수 있습니다");
        }

        // 이미 학습완료 상태인 이력인지 여부
        boolean alreadyCompleted = "COMPLETED".equals(existedlog.getStatus());
        // 이번 요청에서 힌트를 획득했는지 여부
        boolean questCompleted = false;

        // 수정 전 학습이력이 '학습완료'상태가 아니고, 수정 후 학습이력이 '학습완료' 상태일 경우
        // 경험치 추가 및 도전과제 달성여부 확인& 업데이트 메소드 호출
        if (!alreadyCompleted && "COMPLETED".equals(updateStudyLogRequest.getStatus())) {

            ExpProcessingDto expProcessingDto = ExpProcessingDto.builder()
                .userNo(userNo)
                .contentType(ContentTypeEnum.STUDY.name())
                .contentNo(existedlog.getDailyStudyLogNo())
                .expGained(20)
                .dailyQuestNo(DailyQuestEnum.STUDY_LEARNING.getDailyQuestNo())
                .build();

            expProcessingService.expProcessing(expProcessingDto);
            log.info("오늘의 도전과제를 달성했습니다");
        }

        questCompleted = true;

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
     * 학습퀴즈이력 생성 ㄴ 이미 존재하는 학습이력이 있으면, 수정 로직 호출
     *
     * @param studyQuizLogRequest
     * @param userNo
     * @return
     */
    @Transactional
    public DailyStudyQuizLog createStudyQuizLog(StudyQuizLogRequest studyQuizLogRequest,
        int userNo) {
        log.info("createStudyQuizLog 서비스 실행");

        int studyQuizNo = studyQuizLogRequest.getDailyStudyQuizNo();
        int selectedChoice = studyQuizLogRequest.getSelectedChoice();

        // 사용자번호와 학습퀴즈번호로 이미 존재하는 퀴즈 이력이 있는지 확인
        DailyStudyQuizLog existLog
            = dailyStudyQuizLogMapper.getStudyQuizLogsByUserNoAndStudyQuizNo(userNo, studyQuizNo);

        // 만약 이미 존재하는 퀴즈면 에러 발생 - 프론트에서 수정호출하도록 처리
        if (existLog != null) {
            log.info("이미 학습퀴즈이력이 존재하므로 update로직 호출");
            return updateStudyQuizLog(existLog.getDailyStudyQuizLogNo(), studyQuizLogRequest,
                userNo);
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

        return dailyStudyQuizLogMapper.getStudyQuizLogsByNo(
            dailyStudyQuizLog.getDailyStudyQuizLogNo());
    }

    /**
     * 학습퀴즈이력 수정
     *
     * @param studyQuizLogNo
     * @param studyQuizLogRequest
     * @return
     */
    @Transactional
    public DailyStudyQuizLog updateStudyQuizLog(int studyQuizLogNo,
        StudyQuizLogRequest studyQuizLogRequest, int userNo) {
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
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<StudyDifficulty> getStudyDifficulties() {
        return studyDifficultyMapper.getDifficulties();
    }

    /**
     * 서술형 퀴즈 화면에 필요한 데이터 조회
     *
     * @param dailyStudyNo
     * @param userNo
     */
    @Transactional(readOnly = true)
    public StudyEssayViewDto getStudyEssayView(int dailyStudyNo, Integer userNo) {
        log.info("getStudyEssayView 서비스 실행");

        StudyEssayViewDto dto = new StudyEssayViewDto();

        // 서술형 퀴즈 데이터 조회
        DailyStudyEssayQuiz essay = dailyStudyEssayQuizMapper.getEssayQuizByDailyStudyNo(
            dailyStudyNo);
        dto.setEssay(essay);

        // 사용자의 서술형 퀴즈 이력 조회
        if (userNo != null) {
            DailyStudyEssayQuizLog essayLog = dailyStudyEssayQuizMapper.getEssayQuizLogByQuizNoAndUserNo(
                essay.getDailyStudyEssayQuizNo(), userNo);
            dto.setEssayLog(essayLog);
        }

        return dto;
    }

    /**
     * 서술형 퀴즈 이력 생성
     *
     * @param request
     * @param userNo
     * @return
     */
    @Transactional
    public DailyStudyEssayQuizLog createStudyEssayQuizLog(EssayQuizLogRequest request, int userNo) {
        log.info("createStudyEssayQuizLog 서비스 실행");

        // 이미 존재하는 사용자의 서술형 퀴즈 이력이 존재하는지 확인 후 , 존재하면 업데이트 메소드 호출
        DailyStudyEssayQuizLog existLog = dailyStudyEssayQuizMapper.getEssayQuizLogByQuizNoAndUserNo(
            request.getDailyStudyEssayQuizNo(), userNo);
        if (existLog != null) {
            return updateStudyEssayQuizLog(existLog.getDailyStudyEssayQuizLogNo(), request, userNo);
        }

        // DTO를 VO객체로 변환
        DailyStudyEssayQuizLog essayQuizLog = modelMapper.map(request,
            DailyStudyEssayQuizLog.class);
        essayQuizLog.setUserNo(userNo);

        // 서술형퀴즈 이력 생성 매퍼 호출
        dailyStudyEssayQuizMapper.insertStudyEssayQuizLog(essayQuizLog);

        // 생성된 이력을 조회해서 반환
        return dailyStudyEssayQuizMapper.getEssayQuizLogByNo(
            essayQuizLog.getDailyStudyEssayQuizLogNo());
    }

    /**
     * 서술형 퀴즈 이력 수정
     *
     * @param dailyStudyEssayQuizLogNo
     * @param request
     * @param userNo
     * @return
     */
    @Transactional
    public DailyStudyEssayQuizLog updateStudyEssayQuizLog(int dailyStudyEssayQuizLogNo,
        EssayQuizLogRequest request, int userNo) {
        log.info("updateStudyEssayQuizLog 서비스 실행");

        // 서술형퀴즈이력의 사용자와 현재사용자가 일치하지않으면 권한 에러 발생
        DailyStudyEssayQuizLog essayQuizLog = dailyStudyEssayQuizMapper.getEssayQuizLogByNo(
            dailyStudyEssayQuizLogNo);
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
     *
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
     *
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
     *
     * @param dailyStudyNo
     * @param userNo
     * @return
     */
    @Transactional(readOnly = true)
    public StudyCompleteViewDto getStudyCompleteView(int dailyStudyNo, int userNo) {
        log.info("getStudyCompleteView 서비스 메소드 실행");

        StudyCompleteViewDto dto = new StudyCompleteViewDto();

        DailyStudyLog studyLog = dailyStudyLogMapper.getStudyLogByUserNoAndDailyStudyNo(userNo,
            dailyStudyNo);
        dto.setLog(studyLog);

        return dto;
    }

    /**
     * 사용자의 일일 학습 이력 상세 목록을 조회한다.
     * <p>
     * 페이징 처리(page, rows)에 따라 조회 범위를 계산하고, DB 조회 조건(Map)을 구성하여 Mapper를 호출한다.
     * <p>
     * - SQL에서는 학습 로그(l) 기준으로 JOIN을 수행하며 퀴즈 통계(totalQuizCount, successedQuizCount)와 서술형 제출
     * 여부(essaySubmitted)까지 한 번에 반환한다.
     *
     * @param userNo 조회할 사용자 번호
     * @param page   현재 페이지 번호 (1부터 시작)
     * @param rows   한 페이지당 조회할 행의 개수
     * @return 사용자의 일일 학습 이력 상세 목록 리스트
     */
    public Page<DailyStudyLogDetailResponse> getStudyLogsDetail(int userNo, int page, int rows) {
        log.info("getStudyLogsDetail 서비스 메소드 실행");

        // 페이징 처리에 필요한 객체 생성
        Page<DailyStudyLogDetailResponse> studyLogPage = new Page<>();
        
        // 사용자의 학습 이력 총 개수 조회
        int totalRows = dailyStudyLogMapper.getStudyLogsTotalCount(userNo);

        if (totalRows == 0) {
            // 조회할 데이터 행의 수가 없다면 빈 리스트 반환
            // 불필요한 DB 쿼리 발생 방지
            studyLogPage.setItems(Collections.emptyList());
            return studyLogPage;
        }

        // 페이징 처리 조건
        Pagination pagination = new Pagination(page, totalRows, rows); // 페이지네이션 객체 생성
        Map<String, Object> condition = new HashMap<>();
        condition.put("offset", pagination.getOffset());
        condition.put("rows", pagination.getRows());

        // (2) 조회 조건 맵 구성
        // SQL에서 사용할 파라미터로 전달됨 (MyBatis의 @Param("condition") 매핑)
//        Map<String, Object> dailyStudyLogCondition = new HashMap<>();
//        dailyStudyLogCondition.put("rows", rows);
//        dailyStudyLogCondition.put("offset", offset);
//        dailyStudyLogCondition.put("order", "updatedDate");

        // (3) Mapper 호출
        // - SQL 내부에서 JOIN을 통해 아래 정보를 한 번에 조회
        //   ① 학습 기본 정보 (제목, 상태, 카드 수)
        //   ② 객관식 퀴즈 통계 (총 개수, 정답 개수)
        //   ③ 서술형 퀴즈 제출 여부
        List<DailyStudyLogDetailResponse> dailyStudyLogs = dailyStudyLogMapper.getStudyLogsDetailByUserNo(
            userNo, condition);

        // 페이지네이션 데이터 목록 세팅
        studyLogPage.setCondition(condition);
        studyLogPage.setItems(dailyStudyLogs);
        studyLogPage.setPagination(pagination);

        // (4) 결과 반환
        return studyLogPage;
    }

    /**
     * 해당 사용자의 학습 이력 개수를 조회해서 반환하는 서비스
     *
     * @param userNo
     * @return
     */
    public int getStudyLogCount(int userNo) {
        log.info("getStudyLogCount 서비스 메소드 실행");

        return dailyStudyLogMapper.getStudyLogsTotalCount(userNo);
    }

    /**
     * 그룹의 학습자료 업로드 요청을 처리하는 서비스 파일을 스토리지에 저장 후, Redis 작업큐에 파일정보를 push하면 파이썬 워커가 해당 작업큐의 데이터로 파일에서
     * 텍스트를 추출하여 반환(콜백 API 호출) - 파일을 Object Storage에 저장 - DB의 dailyStudyMaterials 테이블에 데이터 추가
     * (원본파일명, 스토리지 경로) - JobStatusResponse 객체 생성 및 Redis 상태저장소에 저장 - Redis 작업 큐에 push
     *
     * @param files
     */
    public List<String> uploadAdminMaterials(List<MultipartFile> files, String school, int grade) {
        List<String> jobIdList = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // 1. Object Storage에 자료 업로드
                String fileUrl = objectStorageService.uploadAdminMaterial(file, school, grade);

                // 2. DB에 파일 정보 저장 (dailyStudyMaterials테이블)
                String filename = file.getOriginalFilename();

                DailyStudyMaterial material = new DailyStudyMaterial();
                material.setSourceFilename(filename);
                material.setFilePath(fileUrl);
                material.setSchool(school);
                material.setGrade(grade);
                int lastSequence = dailyStudyMaterialMapper.getMaxSequenceBySchoolAndGrade(school,
                    grade);
                material.setSequence(lastSequence + 1);

                dailyStudyMaterialMapper.insertStudyMaterial(material);

                // 3. jobid 생성
                String jobId = String.format("admin:material:%d",
                    material.getDailyStudyMaterialNo());

                // 4. JobStatusResponse 생성 및 Redis 저장
                // Job 상태 객체 생성 (PROCESSING 상태)
                JobStatusResponse<AdminParseMaterialResponse> jobStatus = JobStatusResponse.<AdminParseMaterialResponse>builder()
                    .jobId(jobId)
                    .status("PENDING")
                    .progress(10)
                    .message("관리자 학습자료 업로드 완료, 파싱 대기 중")
                    .build();
                // Redis 상태저장소에 저장
                adminParseJobStatusStore.put(jobStatus);

                // group:{groupNo}:jobs 세트에 jobId 추가 (보조 인덱스)
                stringRedisTemplate.opsForSet().add("admin:jobs", jobId);

                // 여기서 Set에도 TTL 적용
                // 그룹 내 작업이 계속 들어오면 set이 유지되고, 일정 시간(15분) 동안 아무 작업도 없으면 자동 삭제
                stringRedisTemplate.expire("admin:jobs", Duration.ofMinutes(30));

                // 5. 작업큐에 push
                StudyMaterialJobPayload payload = StudyMaterialJobPayload.builder()
                    .jobId(jobId)
                    .fileUrl(fileUrl)
                    .school(school)
                    .grade(grade)
                    .dailyStudyMaterialNo(material.getDailyStudyMaterialNo())
                    .build();
                String payloadJson = objectMapper.writeValueAsString(payload);

                stringRedisTemplate.opsForList().leftPush("queue:material:admin", payloadJson);

                jobIdList.add(jobId);
            } catch (IOException e) {
                throw new AppException("첨부파일 저장 중 오류가 발생하였습니다");
            }
        }

        return jobIdList;
    }

    public void handleAdminWorkerCallback2(WorkerMaterialCallbackRequest request) {
        int dailyStudyMaterialNo = request.getDailyStudyMaterialNo();

        // 1. 추출된 텍스트를 dailyStudyMaterial의 content에 저장
        DailyStudyMaterial material = DailyStudyMaterial.builder()
            .dailyStudyMaterialNo(dailyStudyMaterialNo)
            .content(request.getContent())
            .build();
        dailyStudyMaterialMapper.updateStudyMaterial(material);

        // dailyStudyMaterialNo로 material 조회
        material = dailyStudyMaterialMapper.getStudyMaterialByNo(dailyStudyMaterialNo);

        // JobStatus에 담을 프론트 반환 값 생성
        AdminParseMaterialResponse response = AdminParseMaterialResponse.builder()
            .dailyStudyMaterialNo(dailyStudyMaterialNo)
            .materialTitle(material.getMaterialTitle())
            .school(material.getSchool())
            .grade(material.getGrade())
            .sourceFilename(material.getSourceFilename())
            .build();

        // 2. 작업상태 업데이트
        adminParseJobStatusStore.update(request.getJobId(), status -> {
            status.setStatus("DONE");
            status.setProgress(100);
            status.setResult(null);
            status.setMessage("텍스트 추출 완료");
        });
    }

}