package com.kukokuk.rest;

import com.kukokuk.dto.DictationQuestionLogDto;
import com.kukokuk.response.ApiResponse;
import com.kukokuk.response.ResponseEntityUtils;
import com.kukokuk.service.DictationService;
import com.kukokuk.vo.DictationQuestion;
import com.kukokuk.vo.DictationQuestionLog;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dictation")
public class ApiDictationController {

    private final DictationService dictationService;

    /**
     * 받아쓰기 문제와 힌트들 ajax로 가져오기
     * @param dictationQuestionNo 문제 번호
     * @return 받아쓰기 문제, 힌트1, 힌트2, 힌트3
     */
    @GetMapping("/question")
    public ResponseEntity<ApiResponse<DictationQuestion>> questionApi(@RequestParam Integer dictationQuestionNo) {
        log.info("questionApi() 컨트롤러 실행");
        DictationQuestion dictationQuestion = dictationService.getDictationQuestionByQuestionNo(dictationQuestionNo);
        return ResponseEntityUtils.ok(dictationQuestion);
    }

    /**
     * 받아쓰기 세트 번호를 기준으로 해당 세트의 문제 풀이 이력을 조회
     *
     * @param dictationSessionNo 받아쓰기 세트 번호
     * @return 해당 세트에 대한 문제 풀이 이력 목록
     */
    @GetMapping("/results/{sessionNo}/logs")
    public ResponseEntity<ApiResponse<List<DictationQuestionLog>>> getLogs(
        @PathVariable("sessionNo") int dictationSessionNo) {
        log.info("세트번호: {}", dictationSessionNo);
        List<DictationQuestionLog> logs = dictationService.getLogsBySessionNo(dictationSessionNo);
        log.info("이력 조회 성공 - 총 {}개 로그 반환", logs.size());
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
