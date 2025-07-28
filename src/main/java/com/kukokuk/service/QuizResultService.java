package com.kukokuk.service;

import com.kukokuk.dto.QuizResultResponse;
import com.kukokuk.mapper.QuizResultMapper;
import com.kukokuk.vo.QuizResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class QuizResultService {

    private final QuizResultMapper quizResultMapper;

    /**
     * 퀴즈 문제별 결과 저장, 통계 갱신
     * @param result 퀴즈 결과 객체
     */
    @Transactional
    public void insertQuizResult(QuizResult result) {
        //퀴즈 결과 저장
        int insertedRows = quizResultMapper.insertQuizResult(result);
        if (insertedRows != 1) {
            throw new RuntimeException("퀴즈 결과 저장 실패: quizNo=" + result.getQuizNo());
        }
        log.info("퀴즈 결과 저장 성공");

        // 문제 풀이 횟수 업데이트
        int usageUpdated = quizResultMapper.updateUsageCount(result.getQuizNo());
        if (usageUpdated != 1) {
            throw new RuntimeException("문제 풀이 횟수 업데이트 실패: quizNo=" + result.getQuizNo());
        }
        log.info("문제 풀이 횟수 업데이트");
        //정답인 경우 성공 횟수 업데이트
        if ("Y".equals(result.getIsSuccess())) {
            int successUpdated = quizResultMapper.updateSuccessCount(result.getQuizNo());
            if (successUpdated != 1) {
                throw new RuntimeException("문제 성공 횟수 업데이트 실패: quizNo=" + result.getQuizNo());
            }
            log.info("문제 풀이 정답 횟수 업데이트");
        }
    }

    /**
     * 특정 세션의 퀴즈 결과 목록을 조회한다.
     * @param sessionNo 세션 번호
     * @param userNo 사용자 번호
     * @return 퀴즈 결과 응답 리스트
     */
    public List<QuizResultResponse> getQuizResultsBySession(int sessionNo, int userNo) {
        return quizResultMapper.getQuizResultsBySession(sessionNo, userNo);
    }
}
