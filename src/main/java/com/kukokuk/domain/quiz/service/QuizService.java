package com.kukokuk.domain.quiz.service;

import com.kukokuk.domain.quiz.dto.QuizHistoryDto;
import com.kukokuk.domain.quiz.dto.QuizLevelResultDto;
import com.kukokuk.domain.quiz.mapper.DictEntryMapper;
import com.kukokuk.domain.quiz.mapper.QuizMasterMapper;
import com.kukokuk.domain.quiz.mapper.QuizHistoryMapper;  // 🔧 추가
import com.kukokuk.domain.quiz.vo.DictEntry;
import com.kukokuk.domain.quiz.vo.QuizMaster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizMasterMapper quizMasterMapper;
    private final DictEntryMapper dictEntryMapper;
    private final QuizHistoryMapper quizHistoryMapper;  // 🔧 추가

    /**
     * 사전 데이터에서 무작위 단어 1개로 퀴즈를 생성하고 저장한다.
     */
    public void insertQuizByWordRandomEntry(int count) {
        List<DictEntry> correctEntries = dictEntryMapper.getRandomDictEntries(count);

        List<String> correctWords = new ArrayList<>();
        for (DictEntry entry : correctEntries) {
            correctWords.add(entry.getWord());
        }

        int limit = correctEntries.size() * 3;
        List<DictEntry> optionEntries = dictEntryMapper.getRandomWordExclude(correctWords, limit);

        for (int i = 0; i < correctEntries.size(); i++) {
            DictEntry correct = correctEntries.get(i);

            // optionEntries에서 i * 3부터 i * 3 + 3까지 3개 가져오기
            List<DictEntry> options = optionEntries.subList(i * 3, i * 3 + 3);

            options.add(correct);
            Collections.shuffle(options);
            int correctIndex = -1;
            for (int j = 0; j < options.size(); j++) {
                if (options.get(j).getEntryNo() == correct.getEntryNo()) {
                    correctIndex = j + 1;
                    break;
                }
            }
            QuizMaster quiz = new QuizMaster();
            quiz.setEntryNo(correct.getEntryNo());
            log.info("생성된 EntryNo: {}", correct.getEntryNo());
            quiz.setQuestion(correct.getDefinition());
            quiz.setOption1(options.get(0).getWord());
            quiz.setOption2(options.get(1).getWord());
            quiz.setOption3(options.get(2).getWord());
            quiz.setOption4(options.get(3).getWord());
            quiz.setSuccessAnswer(correctIndex);
            quiz.setQuestionType("뜻");

            quizMasterMapper.insertQuiz(quiz);
            correctIndex = 0;
        }
    }

    public void insertQuizByDefRandomEntry(int count) {
        List<DictEntry> correctEntries = dictEntryMapper.getRandomDictEntries(count);

        List<String> correctWords = new ArrayList<>();
        for (DictEntry entry : correctEntries) {
            correctWords.add(entry.getWord());
        }

        int limit = correctEntries.size() * 3;
        List<DictEntry> optionEntries = dictEntryMapper.getRandomWordExclude(correctWords, limit);

        for (int i = 0; i < correctEntries.size(); i++) {
            DictEntry correct = correctEntries.get(i);

            // optionEntries에서 i * 3부터 i * 3 + 3까지 3개 가져오기
            List<DictEntry> options = optionEntries.subList(i * 3, i * 3 + 3);

            options.add(correct);
            Collections.shuffle(options);
            int correctIndex = -1;
            for (int j = 0; j < options.size(); j++) {
                if (options.get(j).getEntryNo() == correct.getEntryNo()) {
                    correctIndex = j + 1;
                    break;
                }
            }
            QuizMaster quiz = new QuizMaster();
            quiz.setEntryNo(correct.getEntryNo());
            log.info("생성된 EntryNo: {}", correct.getEntryNo());
            quiz.setQuestion(correct.getWord());
            quiz.setOption1(options.get(0).getDefinition());
            quiz.setOption2(options.get(1).getDefinition());
            quiz.setOption3(options.get(2).getDefinition());
            quiz.setOption4(options.get(3).getDefinition());
            quiz.setSuccessAnswer(correctIndex);
            quiz.setQuestionType("단어");

            quizMasterMapper.insertQuiz(quiz);
            correctIndex = 0;
        }
    }


    /**
     * 최초 1회만 실행되는 함수
     * 사전 데이터의 랜덤한 단어를 기반으로 서로 다른 유형의 퀴즈를 200개 생성한다.
     * 생성하려는 퀴즈 count의 수를 기반으로 퀴즈의 수가 모자란 만큼만 생성
     * @param count 생성할 퀴즈 수
     */
    public void insertRandomQuizBulk(int count) {
        int quizcount = quizMasterMapper.getQuizCount();
        if (quizcount < count) {

            insertQuizByWordRandomEntry(count-quizcount);
            insertQuizByDefRandomEntry(count-quizcount);
        } else {
            log.info("퀴즈 생성 도중 실패 ");

        }
    }
    //원하는 퀴즈 유형을 임의로 생성하고 싶을 때 사용하는 함수
    public void insertRandomTypeQuizBulk(int usageCount) {
        final int targetCount = 100;

        // 뜻 유형
        int meaningCount = quizMasterMapper.getQuizCountByTypeAndUsageCount("뜻", usageCount);
        int meaningToCreate = Math.max(0, targetCount - meaningCount);
        if (meaningToCreate > 0) {
            insertQuizByWordRandomEntry(meaningToCreate);
            log.info("생성된 뜻 유형 퀴즈 수 : {}", meaningToCreate);
        }

        // 단어 유형
        int wordCount = quizMasterMapper.getQuizCountByTypeAndUsageCount("단어", usageCount);
        int wordToCreate = Math.max(0, targetCount - wordCount);
        if (wordToCreate > 0) {
            insertQuizByDefRandomEntry(wordToCreate);
            log.info("생성된 단어유형 퀴즈 수 : {}", wordToCreate);
        }

    }

    /**
     * 스피드 퀴즈용 문제 10개 조회
     * @param usageCount 기준 usage_count
     * @param limit 개수 제한
     * @return QuizMaster 리스트
     */
    public List<QuizMaster> getSpeedQuizList(int usageCount, int limit) {
        return quizMasterMapper.getQuizMastersForSpeed(usageCount, limit);
    }

    /**
     * 단계별 퀴즈용 문제 10개 조회
     *
     * @param difficulty 난이도 ("쉬움", "보통", "어려움")
     * @param questionType 문제 유형 ("뜻", "단어")
     * @return QuizMaster 리스트
     */
    public List<QuizMaster> getLevelQuizList(String difficulty, String questionType) {
        log.info("단계별 퀴즈 요청 - 난이도: {}, 유형: {}", difficulty, questionType);
        return quizMasterMapper.getQuizListByDifficultyAndType(difficulty, questionType);
    }

    /**
     * 세션 번호로 DIFFICULTY, QUESTION_TYPE을 조회한다.
     * @param sessionNo 세션 번호
     * @return QuizLevelResultdto
     */
    public QuizLevelResultDto getDifficultyAndQuestionTypeBySessionNo(int sessionNo) {
        return quizMasterMapper.getDifficultyAndQuestionTypeBySessionNo(sessionNo);
    }

    /**
     * 사용자 번호로 스피드 퀴즈 최근 이력을 조회한다.
     *
     * @param userNo 사용자 번호
     * @param limit 조회할 개수
     * @return 스피드 퀴즈 이력 리스트
     */
    public List<QuizHistoryDto> getSpeedHistoryByUserNoWithLimit(int userNo, int limit) {
        log.info("[Service] 스피드 퀴즈 이력 조회 - userNo: {}, limit: {}", userNo, limit);

        try {
            List<QuizHistoryDto> historyList = quizHistoryMapper.getSpeedHistoryByUserNoWithLimit(userNo, limit);
            log.info("[Service] 스피드 퀴즈 이력 조회 성공 - 조회된 개수: {}", historyList.size());
            return historyList;
        } catch (Exception e) {
            log.error("[Service] 스피드 퀴즈 이력 조회 실패 - userNo: {}", userNo, e);
            throw new RuntimeException("스피드 퀴즈 이력 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자 번호로 단계별 퀴즈 최근 이력을 조회한다.
     *
     * @param userNo 사용자 번호
     * @param limit 조회할 개수
     * @return 단계별 퀴즈 이력 리스트
     */
    public List<QuizHistoryDto> getLevelHistoryByUserNoWithLimit(int userNo, int limit) {
        log.info("[Service] 단계별 퀴즈 이력 조회 - userNo: {}, limit: {}", userNo, limit);

        try {
            List<QuizHistoryDto> historyList = quizHistoryMapper.getLevelHistoryByUserNoWithLimit(userNo, limit);
            log.info("[Service] 단계별 퀴즈 이력 조회 성공 - 조회된 개수: {}", historyList.size());
            return historyList;
        } catch (Exception e) {
            log.error("[Service] 단계별 퀴즈 이력 조회 실패 - userNo: {}", userNo, e);
            throw new RuntimeException("단계별 퀴즈 이력 조회 중 오류가 발생했습니다.", e);
        }
    }
/*
    *//*
     * 사용자 번호로 퀴즈 이력 전체(스피드 + 단계별)를 조회한다.
     * 최근 이력 순으로 정렬하여 반환한다.
     * 추후에 필요할 시 사용
     * @param userNo 사용자 번호
     * @param limit 조회할 개수
     * @return 퀴즈 이력 리스트 (스피드 + 단계별 통합)
     *//*
    public List<QuizHistoryDto> getQuizHistoryByUserNoWithLimit(int userNo, int limit) {
        log.info("[Service] 퀴즈 이력 전체 조회 - userNo: {}, limit: {}", userNo, limit);

        try {
            // 스피드와 단계별 이력을 각각 조회
            List<QuizHistoryDto> speedHistory = quizHistoryMapper.getSpeedHistoryByUserNoWithLimit(userNo, limit);
            List<QuizHistoryDto> levelHistory = quizHistoryMapper.getLevelHistoryByUserNoWithLimit(userNo, limit);

            // 두 리스트를 합치고 생성일 기준으로 최신순 정렬
            List<QuizHistoryDto> allHistory = new ArrayList<>();
            allHistory.addAll(speedHistory);
            allHistory.addAll(levelHistory);

            // 생성일 기준 내림차순 정렬 후 limit 개수만큼 자르기
            return allHistory.stream()
                .sorted((h1, h2) -> h2.getCreatedDate().compareTo(h1.getCreatedDate()))
                .limit(limit)
                .toList();

        } catch (Exception e) {
            log.error("[Service] 퀴즈 이력 전체 조회 실패 - userNo: {}", userNo, e);
            throw new RuntimeException("퀴즈 이력 조회 중 오류가 발생했습니다.", e);
        }
    }
    */
}