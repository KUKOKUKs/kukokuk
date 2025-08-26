package com.kukokuk.domain.dictation.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.dictation.dto.DictationQuestionLogDto;
import com.kukokuk.domain.dictation.dto.DictationResultLogDto;
import com.kukokuk.domain.dictation.service.DictationService;
import com.kukokuk.domain.dictation.vo.DictationQuestion;
import com.kukokuk.domain.dictation.vo.DictationQuestionLog;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

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

    @PostMapping("/use-hint")
    public ResponseEntity<ApiResponse<Void>> useHint(@RequestParam Integer dictationQuestionNo,
        @SessionAttribute(value = "dictationQuestionLogDto", required = false) List<DictationQuestionLogDto> dictationQuestionLogDtoList) {

        log.info("[/use-hint] 요청 받음 - dictationQuestionNo: {}", dictationQuestionNo);

        for (DictationQuestionLogDto dictationQuestionLogDto : dictationQuestionLogDtoList) {
            if (dictationQuestionLogDto.getDictationQuestionNo() == dictationQuestionNo) {
                dictationQuestionLogDto.setUsedHint("Y");
                log.info("usedHint: {}", dictationQuestionLogDto.getUsedHint());
                break;
            }
        }
        return ResponseEntityUtils.ok("힌트 사용 완료");
    }

    /**
     * 받아쓰기 세트 번호를 기준으로 해당 세트의 문제 풀이 이력을 조회
     *
     * @param dictationSessionNo 받아쓰기 세트 번호
     * @return 해당 세트에 대한 문제 풀이 이력 목록
     */
    @GetMapping("/results/{sessionNo}/logs")
    public ResponseEntity<ApiResponse<List<DictationResultLogDto>>> getLogs(
        @PathVariable("sessionNo") int dictationSessionNo,
        @AuthenticationPrincipal SecurityUser securityUser) {

        int userNo = securityUser.getUser().getUserNo();
        log.info("세트번호: {}, 사용자: {}", dictationSessionNo, userNo);

        List<DictationResultLogDto> logs = dictationService.getLogsBySessionNo(dictationSessionNo, userNo);

        log.info("이력 조회 성공 - 총 {}개 로그 반환", logs.size());
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
