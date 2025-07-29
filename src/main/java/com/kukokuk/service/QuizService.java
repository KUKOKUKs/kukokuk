package com.kukokuk.service;

import com.kukokuk.mapper.DictEntryMapper;
import com.kukokuk.mapper.QuizMasterMapper;
import com.kukokuk.vo.DictEntry;
import com.kukokuk.vo.QuizMaster;
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
     * 사전 데이터의 랜덤한 단어를 기반으로 서로 다른 유형의 퀴즈를 100개씩 생성한다.
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


}