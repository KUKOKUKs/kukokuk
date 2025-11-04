package com.kukokuk.domain.dictation.controller.api;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.dictation.service.DictationService;
import com.kukokuk.domain.dictation.vo.DictationQuestion;
import com.kukokuk.domain.dictation.vo.DictationSession;
import com.kukokuk.domain.user.service.UserService;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dictation")
public class ApiDictationController {

    private final DictationService dictationService;
    private final UserService userService;

    /**
     * 받아쓰기 문제와 힌트들 ajax로 가져오기
     * @param dictationQuestionNo 문제 번호
     * @return 받아쓰기 문제, 힌트1, 힌트2, 힌트3
     */
    @GetMapping("/question")
    public ResponseEntity<ApiResponse<DictationQuestion>> questionApi(
        @RequestParam Integer dictationQuestionNo
        , @RequestParam(defaultValue = "false") boolean usedHint
        , @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("questionApi() 컨트롤러 실행");
        DictationQuestion dictationQuestion = dictationService.getDictationQuestionByQuestionNo(dictationQuestionNo);
        if (usedHint) {
            userService.minusUserHintCount(securityUser.getUser().getUserNo());
        }
        return ResponseEntityUtils.ok(dictationQuestion);
    }


    /**
     * 받아쓰기 세트 결과 조회
     * @param limit 조회 개수(최대 5개)
     * @param securityUser 사용자
     * @return 받아쓰기 세트 결과
     */
    @GetMapping("/result/sessions")
    public ResponseEntity<ApiResponse<List<DictationSession>>> getResultSessions(
        @RequestParam(defaultValue = "5") int limit,
        @AuthenticationPrincipal SecurityUser securityUser) {

        int userNo = securityUser.getUser().getUserNo();

        List<DictationSession> dictationSession = dictationService.getResultsSessionsByUserNo(userNo, limit);
        log.info("이력 컴포넌트 조회 성공 - 사용자 번호 : {}, 개수: {}", userNo, limit);

        return ResponseEntityUtils.ok(dictationSession);
    }


//    /**
//     * 정답 보기 버튼 누를 시
//     * @param questionIndex 세션에 저장된 현재 인덱스
//     * @param dictationQuestionLogDtoList 세션에 저장된 이력 dto 목록
//     * @return 현재 문제 오답 처리
//     */
//    @PostMapping("/show-answer")
//    @ResponseBody
//    public ResponseEntity<ApiResponse<Void>> showAnswer(
//        @ModelAttribute("questionIndex") int questionIndex,
//        @ModelAttribute("dictationQuestionLogDto") List<DictationQuestionLogDto> dictationQuestionLogDtoList
//    ) {
//        log.info("[@PostMapping(/show-answer)] showAnswer 실행 questionIndex: {}", questionIndex);
//
//        // 정답 보기 사용시 오답 처리, 시도횟수 : 2회, 제출문장: <정답 보기 사용>
//        DictationQuestionLogDto dictationQuestiondto = dictationQuestionLogDtoList.get(questionIndex);
//        dictationService.insertShowAnswerAndSkip(dictationQuestiondto);
//
//        // 변경 후 값 로그 출력
//        log.info("[/show-answer] 변경 후 - tryCount: {}, isSuccess: {}, userAnswer: {} / nextIndex: {}",
//            dictationQuestiondto.getTryCount(), dictationQuestiondto.getIsSuccess(), dictationQuestiondto.getUserAnswer(), questionIndex + 1);
//
//        return ResponseEntityUtils.ok("정답보기 처리 완료");
//    }
//
//    /**
//     * 각 문제 힌트 사용 여부
//     * @param questionIndex 세션에 저장된 현재 인덱스
//     * @param dictationQuestionLogDtoList 세션에 저장된 이력 dto 목록
//     * @return 힌트 사용 여부
//     */
//    @PostMapping("/use-hint")
//    @ResponseBody
//    public ResponseEntity<ApiResponse<Void>> useHint(
//        @RequestParam("hintNum") Integer hintNum,
//        @ModelAttribute("questionIndex") int questionIndex,
//        @ModelAttribute("dictationQuestionLogDto") List<DictationQuestionLogDto> dictationQuestionLogDtoList,
//        @ModelAttribute("dictationQuestions") List<DictationQuestion> dictationQuestions,
//        @AuthenticationPrincipal SecurityUser securityUser
//    ) {
//        log.info("[/use-hint] 실행 - questionIndex: {}", questionIndex);
//
//        //int userNo = securityUser.getUser().getUserNo();
//
//        // 현재 문제만 힌트 사용 처리
//        DictationQuestionLogDto dto = dictationQuestionLogDtoList.get(questionIndex);
//        dto.setUsedHint("Y");
//
//        dictationQuestions.get(questionIndex).setUsedHintNum(hintNum);
//        log.info("[/use-hint] index: {}, usedHint: Y", questionIndex);
//
//        // 힌트 사용 시 유저 힌트 수 -1 차감
//        // userService.minusUserHintCount(userNo);
//
//        return ResponseEntityUtils.ok("힌트 사용 완료");
//    }
}
