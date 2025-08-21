package com.kukokuk.domain.quest.controller.view;

import com.kukokuk.domain.quest.dto.DailyQuestStatusDto;
import com.kukokuk.domain.quest.service.DailyQuestService;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/daily-quests")
public class DailyQuestController {

    private final ModelMapper modelMapper;
    private final DailyQuestService dailyQuestService;

    // 일일 도전과제 페이지
    @GetMapping
    public String dailyQuests(
        @AuthenticationPrincipal SecurityUser securityUser
        , Model model) {
        log.info("dailyQuests() 컨트롤러 실행");

        // 사용자의 모든 일일도전과제 현황 목록(일일도전과제목록, 진행도, 보상수령여부) 조회
        List<DailyQuestStatusDto> dailyQuestStatusDtos = dailyQuestService.getDailyQuestsStatus(securityUser.getUser().getUserNo());

        // 완료된 일일 도전과제 개수 계산
        int successCount = (int) dailyQuestStatusDtos.stream()
            .filter(DailyQuestStatusDto::isSucceed)
            .count();

        // isObtained == "Y"인 개수 계산
        int obtainedCount = (int) dailyQuestStatusDtos.stream()
            .filter(dto -> "Y".equals(dto.getIsObtained()))
            .count();

        model.addAttribute("dailyQuestStatusDtos", dailyQuestStatusDtos);
        model.addAttribute("successCount", successCount);
        model.addAttribute("obtainedCount", obtainedCount);

        return "quest/main";
    }

}
