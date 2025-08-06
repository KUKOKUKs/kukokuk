package com.kukokuk.service;

import com.kukokuk.mapper.DictationQuestionLogMapper;
import com.kukokuk.mapper.DictationQuestionMapper;
import com.kukokuk.mapper.DictationSessionMapper;
import com.kukokuk.vo.DictationQuestion;
import com.kukokuk.vo.DictationQuestionLog;
import com.kukokuk.vo.DictationSession;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DictationService {

    private final DictationQuestionMapper dictationQuestionMapper;
    private final DictationQuestionLogMapper dictationQuestionLogMapper;
    private final DictationSessionMapper dictationSessionMapper;

  /*
    받아쓰기 게임을 시작할 떄 받아쓰기 문제에서 랜덤으로 10개를 가져와 받아쓰기 세트에 담기
   */

    public DictationSession startDictationSession(int userNo) {
        // 1. 받아쓰기 세트 생성
        DictationSession session = new DictationSession();
        session.setUserNo(userNo);
        session.setStartDate(new Date());
        dictationSessionMapper.insertDictationSession(session);

        // 2. 기존 DB 저장된 문제 랜덤 10개 가져오기
        List<DictationQuestion> randomQuestions = dictationQuestionMapper.getRandomQuestions(10);

        // 3. 받아쓰기 문제 풀이 이력과 연결(DictationSessionNo으로 문제 가져오기, 이력 10개 생성)
        for (DictationQuestion question : randomQuestions) {
            DictationQuestionLog log = new DictationQuestionLog();
            log.setDictationSessionNo(session.getDictationSessionNo());
            log.setDictationQuestionNo(question.getDictationQuestionNo());
            dictationQuestionLogMapper.insertLog(log);
        }

        return session;
    }

    /*
      받아쓰기 끝났을 때 받아쓰기 문제 세트 정답 점수, 맞은 개수, 사용한 힌트 수 결과 반영
     */
    public void finishDictationSession(int dictationSessionNo) {
        // 1. 받아쓰기 문제 풀이 이력 테이블에서 정답 수, 힌트 사용 수 조회
        int correctCount = dictationQuestionLogMapper.getcountCorrectAnswers(dictationSessionNo);
        int correctScore = correctCount * 10; // 문제당 10점(임의)
        int hintUsedCount = dictationQuestionLogMapper.getcountHintsUsed(dictationSessionNo);

        // 2. 받아쓰기 세트 결과 반영용 객체 생성
        DictationSession session = new DictationSession();
        session.setDictationSessionNo(dictationSessionNo);
        session.setCorrectCount(correctCount);
        session.setCorrectScore(correctScore);
        session.setHintUsedCount(hintUsedCount);

        // 3. 받아쓰기 세트 결과 반영
        dictationSessionMapper.updateDictationSessionResult(session);

    }

    /**
     * 받아쓰기 세트 번호로 받아쓰기 세트 가져오기(결과용)
     *
     * @param dictationSessionNo 문제 세트 번호
     * @return 받아쓰기 세트
     */
    public DictationSession getDictationSessionByNo(int dictationSessionNo) {
        return dictationSessionMapper.getDictationSessionByNo(dictationSessionNo);
    }

    /**
     * 힌트 사용
     *
     * @param dictationQuestionLogNo 식별자
     */
    public void useHint(int dictationQuestionLogNo) {
        dictationQuestionLogMapper.updateHintUsed(dictationQuestionLogNo, "Y");
    }

    /**
     * 받아쓰기 문제 수정
     *
     * @param dictationQuestion 문제
     */
    public void updateDictationQuestion(DictationQuestion dictationQuestion) {
        dictationQuestionMapper.updateDictationQuestion(dictationQuestion);
    }

    /**
     * 받아쓰기 문제 삭제
     *
     * @param dictationQuestionNo 문제 번호
     */
    public void deleteDictationQuestion(int dictationQuestionNo) {
        dictationQuestionMapper.deleteDictationQuestion(dictationQuestionNo);
    }
}
