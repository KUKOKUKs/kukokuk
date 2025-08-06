package com.kukokuk.controller;

import com.kukokuk.service.DictationService;
import com.kukokuk.vo.DictationSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dictation")
public class DictationController {

    private final DictationService dictationService;

    /**
     * 새로운 받아쓰기 세트를 생성하고, 랜덤 10문제를 담아 문제 푸는 화면으로 이동
     *
     * @param userNo 회원번호
     * @param model  받아쓰기 세트를 담을 모델
     * @return 받아쓰기 문제 풀이 화면
     */
    @PostMapping("/start")
    public String startDictationSession(@RequestParam("userNo") int userNo, Model model) {
        DictationSession session = dictationService.startDictationSession(userNo);
        model.addAttribute("session", session);
        return "dictation/play";
    }

    /**
     * 받아쓰기 세트 번호로 받아쓰기 세트의 결과를 조회
     *
     * @param dictationsessionNo 문제 세트 번호
     * @return 받아쓰기 세트의 결과
     */
    @PostMapping("/finish")
    public String finishDictationSession(
        @RequestParam("dictationSessionNo") int dictationsessionNo) {
        dictationService.finishDictationSession(dictationsessionNo);
        return "redirect:/dictation/result?dictationsessionNo=" + dictationsessionNo;
    }

    /**
     * 받아쓰기 문제 결과 확인용 받아쓰기 세트 가져오기
     *
     * @param dictationSessionNo 문제 세트 번호
     * @param model              받아쓰기 세트를 담을 모델
     * @return 받아쓰기 문제 풀이 결과 화면
     */
    @GetMapping("/result")
    public String resultDictationSession(@RequestParam("dictationSessionNo") int dictationSessionNo,
        Model model) {
        DictationSession session = dictationService.getDictationSessionByNo(dictationSessionNo);
        model.addAttribute("session", session);
        return "dictation/result";
    }
}
