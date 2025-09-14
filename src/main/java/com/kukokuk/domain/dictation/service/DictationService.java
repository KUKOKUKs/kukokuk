package com.kukokuk.domain.dictation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kukokuk.ai.GeminiClient;
import com.kukokuk.common.exception.AppException;
import com.kukokuk.domain.dictation.dto.DictationQuestionLogDto;
import com.kukokuk.domain.dictation.dto.DictationResultLogDto;
import com.kukokuk.domain.dictation.dto.DictationResultSummaryDto;
import com.kukokuk.domain.dictation.dto.DictationResultsDto;
import com.kukokuk.domain.dictation.mapper.DictationQuestionLogMapper;
import com.kukokuk.domain.dictation.mapper.DictationQuestionMapper;
import com.kukokuk.domain.dictation.mapper.DictationSessionMapper;
import com.kukokuk.domain.dictation.vo.DictationQuestion;
import com.kukokuk.domain.dictation.vo.DictationQuestionLog;
import com.kukokuk.domain.dictation.vo.DictationSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class DictationService {

    private final DictationQuestionMapper dictationQuestionMapper;
    private final DictationQuestionLogMapper dictationQuestionLogMapper;
    private final DictationSessionMapper dictationSessionMapper;
    private final GeminiClient geminiClient;

    /**
     * Gemini를 통해 문장을 받아와서 힌트들을 생성하고 DB에 저장하는 메소드
     *
     * @param count 생성할 문장 수
     */
    @Transactional
    public void insertGenerateAiQuestions(int count) {

        int failCount = 0; // 중복으로 데이터베이스 엑세스 실패 횟수

        // List<String> excludeList = new ArrayList<>();

        // [1] Gemini에게 받아쓰기용 짧고 쉬운 문장들을 요청하는 프롬프트 생성
        String prompt = """
            당신은 받아쓰기 문제 문장만 생성하는 도구입니다. 다음 규칙을 **엄격히** 준수하세요.
        
            1) 출력은 반드시 다음 형식 하나만 한 줄로 출력합니다:
            <sentences_json>["문장1","문장2",...,"문장N"]</sentences_json>
        
            2) %d에 맞춰 정확히 %d개의 문장을 생성하세요.
        
            3) 각 문장은 한국어 평문으로 작성하되 문장부호(.,!?;:·…「」”“''()[]{}—- 등 모든 구두점)를 절대 포함하지 마세요. (띄어쓰기/공백은 허용)
        
            4) 각 문장은 공백 포함 최소 15자, 최대 30자 길이로 작성하세요.
        
            5) 난이도(쉬움/보통/어려움)는 각 문장마다 무작위로 선택하되 난이도 레이블은 절대 출력하지 마세요.
        
            6) JSON 규격을 지켜 쌍따옴표(")로 문자열을 감싸세요. single quote(') 사용 금지.
        
            7) 태그 바깥에는 어떠한 설명이나 추가 텍스트를 출력하지 마세요. 태그와 JSON 배열 외의 출력은 허용되지 않습니다.
        
            8) 출력 예시 (count=3):
            <sentences_json>["나는 오늘도 아침에 학교에 갔다","고양이가 햇살을 받으며 창가에 앉아있다","바람이 부는 공원에서 친구와 뛰어놀았다"]</sentences_json>
        """.formatted(count, count);

        // [2] Gemini로부터 생성된 문장 결과를 받아옴
         String response = geminiClient.getGeminiResponse(prompt);
         log.info("response: {}", response);

           try {
               // [3] <sentences_json>...</sentences_json> 추출
               Pattern pattern = Pattern.compile("<sentences_json>(.*?)</sentences_json>", Pattern.DOTALL);
               Matcher matcher = pattern.matcher(response);

               if (!matcher.find()) {
                   throw new IllegalStateException("sentences_json 태그를 찾을 수 없습니다");
               }

               String jsonArrayString = matcher.group(1).trim(); // ["문장1", "문장2", ...]

               // [4] JSON 파싱
               ObjectMapper objectMapper = new ObjectMapper();
               List<String> sentences = objectMapper.readValue(jsonArrayString, new TypeReference<>() {});

               // [5] 활용
               for (String sentence : sentences) {
                   log.info("문장: {}", sentence);

                   // 앞뒤 공백 제거
                   sentence = sentence.trim();

                   // 중복 확인 (0이면 중복이 없으므로 기존 로직 실행, 아니면 failCount 증가)
                   if (dictationQuestionMapper.isDuplicateCorrectAnswer(sentence) == 0) {

                       DictationQuestion question = new DictationQuestion();
                       question.setCorrectAnswer(sentence);
                       question.setHint1(getUnderlineHint(sentence));
                       question.setHint2(getInitialConsonantsHint(sentence));
                       question.setHint3(sentence.substring(0, 1));

                       dictationQuestionMapper.insertDictationQuestion(question);

                       log.info("문장 생성 완료 문장: {}", sentence);
                   } else {
                       failCount++;
                       log.info("failcount: {}", failCount);
                   }
               }

               // failCount에 대해
               if (failCount > 0) {
                   insertGenerateAiQuestionsWithExcludeList(failCount, jsonArrayString);
               }
               log.info("중복으로 저장 안 된 문장 개수: {}", failCount);

           } catch (DataAccessException | JsonProcessingException e) {
               log.error("Gemini 응답 처리 중 오류 발생", e);
               throw new AppException("응답 처리 중 오류 발생: " + e.getMessage());
       }
    }

    /**
     * 띄어쓰기 힌트 생성 ("_ _ ___" 형식)
     */
    private String getUnderlineHint(String sentence) {
        StringBuilder hint = new StringBuilder();
        for (char c : sentence.toCharArray()) {
            if (c != ' ') {
                hint.append("_");
            } else {
                hint.append(c);
            }
        }
        return hint.toString().trim();
    }

    /**
     * 초성 힌트 생성 ("ㄴㄴㅇㄴㅎㄱㅇㄱㅌ" 형식)
     */
    private String getInitialConsonantsHint(String sentence) {
        StringBuilder result = new StringBuilder();
        for (char ch : sentence.toCharArray()) {
            if (ch >= 0xAC00 && ch <= 0xD7A3) {
                int uniVal = ch - 0xAC00;
                int initialIndex = uniVal / (21 * 28);
                char initial = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ".charAt(initialIndex);
                result.append(initial);
            }
        }
        return result.toString();
    }

    /**
     * AI(Gemini)에게 받아쓰기 문제 문장들을 생성 요청하는 메서드
     * 단, excludeList에 포함된 문장은 절대 생성하지 않도록 요청함
     *
     */
    public void insertGenerateAiQuestionsWithExcludeList(int firstFailCount, String jsonExcludeString) {
        log.info("firstFailCount: {}, jsonExcludeString: {}", firstFailCount, jsonExcludeString);
        int failCount = 0; // 중복으로 데이터베이스 엑세스 실패 횟수

        // Gemini AI에게 보낼 프롬프트 문자열 작성
        // 1) 정확히 count개 문장을 생성할 것
        // 2) excludeJson에 포함된 문장은 생성하지 말 것
        // 3) 문장부호 제외, 길이 제한, 난이도 레이블 출력 금지 등 세부 규칙 포함
        // 4) 출력 형식은 <sentences_json>[문장배열]</sentences_json> 단 한 줄만 출력
        String prompt = """
            당신은 받아쓰기 문제 문장만 생성하는 도구입니다. 다음 규칙을 **엄격히** 준수하세요.
    
            1) 출력은 반드시 다음 형식 하나만 한 줄로 출력합니다:
            <sentences_json>["문장1","문장2",...,"문장N"]</sentences_json>
    
            2) %d에 맞춰 정확히 %d개의 문장을 생성하세요.
    
            3) 다음 리스트에 포함된 문장은 절대 생성하지 마세요:
            %s
    
            4) 각 문장은 한국어 평문으로 작성하되 문장부호(.,!?;:·…「」”“''()[]{}—- 등 모든 구두점)를 절대 포함하지 마세요. (띄어쓰기/공백은 허용)
    
            5) 각 문장은 공백 포함 최소 15자, 최대 30자 길이로 작성하세요.
    
            6) 난이도(쉬움/보통/어려움)는 각 문장마다 무작위로 선택하되 난이도 레이블은 절대 출력하지 마세요.
    
            7) JSON 규격을 지켜 쌍따옴표(")로 문자열을 감싸세요. single quote(') 사용 금지.
    
            8) 태그 바깥에는 어떠한 설명이나 추가 텍스트를 출력하지 마세요. 태그와 JSON 배열 외의 출력은 허용되지 않습니다.
    
            9) 출력 예시 (count=3):
            <sentences_json>["나는 오늘도 아침에 학교에 갔다","고양이가 햇살을 받으며 창가에 앉아있다","바람이 부는 공원에서 친구와 뛰어놀았다"]</sentences_json>
        """.formatted(firstFailCount, firstFailCount, jsonExcludeString);

        // Gemini AI 서버에 프롬프트를 전송하고 응답을 받음
        String response = geminiClient.getGeminiResponse(prompt);

            try {
                // AI 응답에서 <sentences_json> ... </sentences_json> 태그 사이 내용을 추출하기 위해 정규식 패턴 생성
                Pattern pattern = Pattern.compile("<sentences_json>(.*?)</sentences_json>", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(response);

                // 만약 패턴에 맞는 태그가 없으면 예외 발생
                if (!matcher.find()) {
                    throw new IllegalStateException("sentences_json 태그를 찾을 수 없습니다");
                }

                // 태그 내부 JSON 배열 문자열 추출 및 앞뒤 공백 제거
                String jsonArrayString = matcher.group(1).trim();
                log.info("jsonArrayString: {}", jsonArrayString);

                // JSON 배열 문자열을 List<String> 타입으로 파싱
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> sentences = objectMapper.readValue(jsonArrayString, new TypeReference<>() {});

                for (String sentence : sentences) {
                    log.info("문장: {}", sentence);

                    // 앞뒤 공백 제거
                    sentence = sentence.trim();

                    // 중복 확인 (0이면 중복이 없으므로 기존 로직 실행, 아니면 failCount 증가)
                    if (dictationQuestionMapper.isDuplicateCorrectAnswer(sentence) == 0) {

                        DictationQuestion question = new DictationQuestion();
                        question.setCorrectAnswer(sentence);
                        question.setHint1(getUnderlineHint(sentence));
                        question.setHint2(getInitialConsonantsHint(sentence));
                        question.setHint3(sentence.substring(0, 1));

                        dictationQuestionMapper.insertDictationQuestion(question);
                    } else {
                        failCount++;
                        log.info("failCount: {}", failCount);
                    }
                }

                // failCount에 대해
                if (failCount > 0) {
                    log.info("2nd 중복검사 실행");
                    insertGenerateAiQuestionsWithExcludeList(failCount, jsonArrayString);
                }

            } catch (DataAccessException | JsonProcessingException e) {
                log.error("Gemini 2nd 응답 처리 중 오류 발생", e);
                throw new AppException("2nd 응답 처리 중 오류 발생: " + e.getMessage());
        }
    }



    /**
     * 사용자에게 제공할 받아쓰기 문제 10개를 가져오기 이미 푼 문제는 제외하며, 부족하면 문제를 새로 생성한 뒤 다시 가져온다
     *
     * @param userNo 사용자 번호
     * @return 받아쓰기 문제 리스트 (총 10개)
     */
    @Transactional
    public List<DictationQuestion> getDictationQuestionsByUserNo(int userNo, int count) {
        log.info("getDictationQuestionsByUserNo 서비스 실행");

            try {
                // 1. 사용자 기준, 푼 문제는 제외하고 10문제 가져오기
                List<DictationQuestion> questions = dictationQuestionMapper.getRandomDictationQuestionsExcludeUser(
                    userNo, 10);

                // 2. 10문제 보다 부족한 문제 수세기 (예: 4개 부족)
                if (questions.size() < count) {
                    int toCreate = count - questions.size();

                    // 3. 부족한 수 만큼 새로운 문제 생성 (예: 4개 생성)
                    insertGenerateAiQuestions(toCreate);

                    // 4. 새로 생성한 문제 중에서도 사용자 기준으로 푼 문제는 다시 제외하고 가져오기 (예: 새로 생성된 4개 가져옴)
                    List<DictationQuestion> additional = dictationQuestionMapper.getRandomDictationQuestionsExcludeUser(
                        userNo, toCreate);

                    // 5. 기존에 가져온 문제 리스트에 추가 (예: 새로 생성된 4개 합쳐서 questions에 집어넣어 10개 만들기)
                    questions.addAll(additional);
                }

                return questions;

            } catch (DataAccessException e) {
                log.info("getDictationQuestionsByUserNo 예외처리 실행");
                throw new AppException("10문제를 불러오지 못했습니다.");
        }
    }

    /**
     * 받아쓰기 문제 결과 받아쓰기 세트에 저장
     *
     * @param dictationSessionNo 문제 세트 번호
     * @param userNo 회원 번호
     */
    @Transactional
    public void insertDictationSessionResult(Integer dictationSessionNo, int userNo, Date startDate,
        Date endDate) {
        log.info("insertDictationSessionResult 서비스 실행");
        if (dictationSessionNo == null) {
            log.info("insertDictationSessionResult 예외처리 실행");
            throw new AppException("문제를 불러오지 못했습니다.");
        }

            try {
                // 1. 정답 개수 조회
                int correctCount = dictationQuestionLogMapper.getCountCorrectAnswers(
                    dictationSessionNo);

                // 2. 힌트 사용 횟수 조회
                int hintUsedCount = dictationQuestionLogMapper.getCountHintsUsed(dictationSessionNo);

                // 5. 점수 계산
                int correctScore = correctCount * 10;

                // 6. 세션 저장
                DictationSession session = new DictationSession();
                session.setDictationSessionNo(dictationSessionNo); // 외부에서 받은 세션 번호 사용
                session.setUserNo(userNo);
                session.setStartDate(startDate);
                session.setEndDate(endDate);
                session.setCorrectCount(correctCount);
                session.setHintUsedCount(hintUsedCount);
                session.setCorrectScore(correctScore);

                dictationSessionMapper.updateDictationSessionResult(session);
            } catch (DataAccessException e) {
                throw new AppException("결과를 저장하지 못했습니다.");
        }
    }

    /**
     * 문제 제출 시 이력에 저장
     *
     * @param dictationSessionNo  문제 세트 번호
     * @param dictationQuestionNo 문제 번호
     * @param userAnswer          제출 문장
     */
    @Transactional
    public void insertSubmitAnswer(int userNo, int dictationSessionNo, int dictationQuestionNo,
        String userAnswer, String usedHint, int tryCount) {
        log.info("insertSubmitAnswer 서비스 실행");
        if (dictationSessionNo <= 0 || dictationQuestionNo <= 0) {
            throw new AppException("이력 저장에 실패하였습니다");
        }

            try {
                // 1. 정답 문장 가져오기
                String correctAnswer = dictationQuestionMapper.getCorrectAnswerByQuestionNo(
                    dictationQuestionNo);

                // 2. 기존 제출 이력 확인
                DictationQuestionLog existingLog = dictationQuestionLogMapper.getLogBySessionAndQuestion(
                    dictationSessionNo, dictationQuestionNo);

                // 3. 제출 이력 저장
                if (existingLog == null) {
                    // 첫 제출 → INSERT
                    DictationQuestionLog newLog = new DictationQuestionLog();
                    newLog.setUserNo(
                        userNo);                                                       // 사용자 번호
                    newLog.setDictationSessionNo(
                        dictationSessionNo);                               // 문제 세트 번호
                    newLog.setDictationQuestionNo(
                        dictationQuestionNo);                             // 문제 번호
                    newLog.setUserAnswer(
                        userAnswer);                                               // 사용자 제출 답안
                    newLog.setTryCount(
                        tryCount);                                                   // 첫 시도
                    newLog.setIsSuccess(
                        insertIsCorrectAnswer(userAnswer, correctAnswer) ? "Y" : "N");    // 정답 여부
                    newLog.setUsedHint(
                        usedHint);                                                   // 힌트 사용 여부

                    dictationQuestionLogMapper.insertDictationQuestionLog(newLog);
                } else {
                    // 두 번째 제출 → UPDATE
                    existingLog.setUserAnswer(
                        userAnswer);                                           // 새로 제출한 답안
                    existingLog.setTryCount(
                        existingLog.getTryCount() + 1);                          // 시도 횟수 증가
                    existingLog.setIsSuccess(
                        insertIsCorrectAnswer(userAnswer, correctAnswer) ? "Y" : "N");// 정답 여부 판정
                    existingLog.setUsedHint(
                        usedHint);                                               // 힌트 사용 여부 갱신

                    dictationQuestionLogMapper.updateDictationQuestionLog(existingLog);
                }
            } catch (DataAccessException e) {
                throw new AppException("답안을 저장하지 못했습니다.");
        }
    }


    /**
     * 사용자 제출 문장과 정답 문장을 비교하여 정답 여부를 판별 문장부호 및 특수문자는 제외하고 한글, 숫자, 띄어쓰기만 남긴 후 비교
     *
     * @param userAnswer 제출문장
     * @param correctAnswer 정답문장
     * @return 두 문장이 같으면 true (정답), 다르면 false (오답)
     */
    public boolean insertIsCorrectAnswer(String userAnswer, String correctAnswer) {
        log.info("insertIsCorrectAnswer 서비스 실행");
        // 제출문장, 정답문장 두 문장 모두 NULL이 아니게 예외처리
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }

        // 받아쓰기 정답 여부 판별을 위해 문장부호 및 특수문자는 제외하고 한글, 숫자, 띄어쓰기만 남김
        String refinedUserAnswer = userAnswer.replaceAll("[^ㄱ-ㅎ가-힣0-9 ]", "");
        String refinedCorrectAnswer = correctAnswer.replaceAll("[^ㄱ-ㅎ가-힣0-9 ]", "");

        // 비교해서 같다(true) 아니면 다르다(false)로 반환
        return refinedUserAnswer.equals(refinedCorrectAnswer);
    }

    /**
     * 사용자의 번호를 기반으로 받아쓰기 세트 객체를 생성하고, 시작일자와 종료일자를 현재 시각으로 설정한 후 DB에 저장
     *
     * @param userNo 사용자 번호
     * @return 생성된 받아쓰기 세트
     */
    public int createDictationSession(int userNo) {
        log.info("createDictationSession 서비스 실행");

            try {
                DictationSession dictationSession = new DictationSession();
                dictationSession.setUserNo(userNo);
                dictationSession.setStartDate(new Date());
                dictationSession.setEndDate(new Date());
                dictationSessionMapper.insertDictationSession(dictationSession);

                if (dictationSession.getDictationSessionNo() == 0) {
                    log.info("createDictationSession 자동으로 세션번호 추가 실패 또는 세션 번호 없음");
                    throw new AppException("세션번호 예외처리에 의해 문제 세트를 생성하지 못했습니다.");
                }
                return dictationSession.getDictationSessionNo(); // MyBatis에서 자동 채번되어 들어간다고 가정
            } catch (DataAccessException e) {
                throw new AppException("문제 세트를 생성하지 못했습니다.");
        }
    }

    /**
     * 받아쓰기 세트 조회
     * @param userNo 사용자 번호
     * @return 받아쓰기 세트
     */
    public List<DictationSession> getResultsByUserNo(int userNo) {
        return dictationSessionMapper.getDictationSessionResultsByUserNo(userNo);
    }

    /**
     * 받아쓰기 세트의 이력 조회
     * @param dictationSessionNo 문제 세트 번호
     * @param userNo 사용자 번호
     * @return 받아쓰기 세트의 이력
     */
    public List<DictationResultLogDto> getLogsBySessionNo(int dictationSessionNo, int userNo) {
        return dictationQuestionLogMapper.getDictationQuestionLogBySessionNo(dictationSessionNo, userNo);
    }

    /**
     * 문제 번호로 받아쓰기 문제를 조회
     * @param dictationQuestionNo 문제 번호
     * @return 받아쓰기 문제
     */
    public DictationQuestion getDictationQuestionByQuestionNo(Integer dictationQuestionNo) {
        log.info("getDictationQuestionByQuestionNo 서비스 실행");
        if (dictationQuestionNo == null) {
            log.info("getDictationQuestionByQuestionNo 예외처리 실행");
            throw new AppException("문제를 불러오지 못했습니다.");
        }

        try {
            return dictationQuestionMapper.getDictationQuestionByDictationQuestionNo(dictationQuestionNo);
        } catch (DataAccessException e) {
            log.info("getDictationQuestionByQuestionNo 예외처리 실행");
            throw new AppException("문제를 불러오지 못했습니다.");
        }
    }

    /**
     * 문제 세트 번호로 받아쓰기 결과 페이지 보여질 받아쓰기 결과 내용 조회
     * @param dictationSessionNo 세트 번호
     * @return 받아쓰기 문제 세트 결과
     */
    public DictationSession getDictationSessionByDictationSessionNo(int dictationSessionNo) {
        if (dictationSessionNo == 0) {
            log.info("getDictationSessionByDictationSessionNo 예외처리 실행");
            throw new AppException("문제 세트를 불러오지 못했습니다.");
        }

        try {
            return dictationSessionMapper.getDictationSessionByDictationSessionNo(dictationSessionNo);
        } catch (DataAccessException e) {
            log.info("getDictationSessionByDictationSessionNo 예외처리 실행");
            throw new AppException("문제 세트를 불러오지 못했습니다.");
        }
    }

    /**
     * 문제 풀이 로그를 DB에 저장
     * @param userNo 사용자 번호
     * @param sessionNo 문제 세트 번호
     * @param dictationQuestions 세션에 저장된 받아쓰기 문제 목록
     * @param dictationQuestionLogDtoList 세션에 저장된 이력 dto 목록
     */
    @Transactional
    public void insertDictationQuestionLogDto(int userNo, int sessionNo,
        List<DictationQuestion> dictationQuestions,
        List<DictationQuestionLogDto> dictationQuestionLogDtoList) {

        log.info("insertDictationQuestionLogDto 실행");

        for (int i = 0; i < dictationQuestions.size(); i++) {
            DictationQuestion q = dictationQuestions.get(i);
            DictationQuestionLogDto dictationQuestionLogDto = dictationQuestionLogDtoList.get(i);

            String userAnswer = dictationQuestionLogDto.getUserAnswer();
            String isSuccess  = dictationQuestionLogDto.getIsSuccess();
            int tryCount      = dictationQuestionLogDto.getTryCount();
            String usedHint   = (dictationQuestionLogDto.getUsedHint() == null) ? "N" : dictationQuestionLogDto.getUsedHint();

            log.info("[saveDictationLogs] 문제{}: 문제 번호: {}, 제출문장: {}, 맞춤 여부: {}, 시도 횟수: {}, 힌트 사용: {}",
                i + 1, q.getDictationQuestionNo(), userAnswer, isSuccess, tryCount, usedHint);

            insertSubmitAnswer(userNo, sessionNo, q.getDictationQuestionNo(), userAnswer, usedHint, tryCount);
        }
    }

    /**
     * 정답 보기 사용시 오답 처리, 시도횟수 : 2회, 제출문장: <정답 보기 사용>
     * @param dictationQuestionLogDto 세션에 저장된 이력 dto
     */
    @Transactional
    public void insertShowAnswerAndSkip(DictationQuestionLogDto dictationQuestionLogDto) {
        dictationQuestionLogDto.setIsSuccess("N");
        dictationQuestionLogDto.setTryCount(2);
        dictationQuestionLogDto.setUserAnswer("<정답 보기 사용>");
    }

    /**
     * 결과 페이지에 전달될 데이터 담기
     * @param dictationSession 받아쓰기 세트
     * @param dictationQuestions 받아쓰기 문제
     * @param currentUserNo 현재 로그인 된 사용자
     * @param dictationSessionNo 받아쓰기 세트 번호
     * @return 결과 페이지에 담길 데이터들
     */
    public DictationResultSummaryDto getDictationResultSummaryDto(DictationSession dictationSession,
        List<DictationQuestion> dictationQuestions, int currentUserNo, int dictationSessionNo) {

        // 다른 사용자이거나 세트 번호 없으면 예외처리
        if (dictationSession == null || dictationSession.getUserNo() != currentUserNo) {
            throw new AppException("다른 사용자의 세트이거나 세트가 없습니다");
        }

        // summary 부분
        // 문제 총 개수
        int totalQuestion = dictationQuestions.size();

        // 문제 맞은 개수
        int correctAnswers = dictationSession.getCorrectCount();

        Date start = dictationSession.getStartDate();
        Date end   = dictationSession.getEndDate();

        // 문제 총 풀이 시간
        long ts = Math.max(0L, end.getTime() - start.getTime()); // 음수 방지
        double totalTimeSec = Math.round((ts / 1000.0) * 1000.0) / 1000.0;

        // 평균 한 문제당 풀이 시간
        double averageTimePerQuestion = Math.round((totalTimeSec / totalQuestion) * 1000.0) / 1000.0;

        DictationResultSummaryDto dictationResultSummaryDto = new DictationResultSummaryDto();
        dictationResultSummaryDto.setTotalQuestion(totalQuestion);
        dictationResultSummaryDto.setCorrectAnswers(correctAnswers);
        dictationResultSummaryDto.setTotalTimeSec(totalTimeSec);
        dictationResultSummaryDto.setAverageTimePerQuestion(averageTimePerQuestion);

        // results 부분
        List<DictationResultLogDto> dictationResultLogDtos =
            dictationQuestionLogMapper.getDictationQuestionLogBySessionNo(dictationSessionNo, currentUserNo);

        List<DictationResultsDto> dictationResultLogDtoList = new ArrayList<>();

        // 문제 푼 세트의 이력 가져오기(문제, 제출문장, 정답여부)
        for (DictationResultLogDto r : dictationResultLogDtos) {
            DictationResultsDto dictationResultsDto = new DictationResultsDto();
            dictationResultsDto.setQuestion(r.getCorrectAnswer());
            dictationResultsDto.setSuccess("Y".equals(r.getIsSuccess()));
            dictationResultsDto.setUserAnswer(r.getUserAnswer());
            dictationResultLogDtoList.add(dictationResultsDto);
        }

        dictationResultSummaryDto.setResults(dictationResultLogDtoList);

        return dictationResultSummaryDto;
    }


}
