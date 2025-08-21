package com.kukokuk.domain.quest.controller.view;

import com.kukokuk.common.exception.AppException;
import com.kukokuk.domain.quest.service.DailyQuestUserService;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/daily-quest-user")
public class DailyQuestUserController {

    private final DailyQuestUserService dailyQuestUserService;

    // 사용자의 완료된 일일 도전과제 보상 획득(일괄) 처리 및 획득 후 힌트 개수 요청
    @PostMapping("/obtain")
    public String dailyQuestAllObtainReward(
        @RequestParam("dailyQuestUserNo") List<Integer> dailyQuestUserNos
        , @AuthenticationPrincipal SecurityUser securityUser
        , RedirectAttributes redirectAttributes) {
        log.info("dailyQuestAllObtainReward() 컨트롤러 실행");

        try {
            // 보상 획득 일괄 처리 요청
            int updatedCount = dailyQuestUserService.updateDailyQuestUserBatch(dailyQuestUserNos
                , securityUser.getUser().getUserNo());

            redirectAttributes.addFlashAttribute(
                "success", "힌트 [" +updatedCount + "]개를 획득했습니다."
            );
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/daily-quests";
    }

}
