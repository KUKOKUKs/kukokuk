package com.kukokuk.domain.rank.controller.view;

import com.kukokuk.common.constant.ContentTypeEnum;
import com.kukokuk.common.constant.PaginationEnum;
import com.kukokuk.common.util.DateUtil;
import com.kukokuk.domain.rank.dto.RankRequestDto;
import com.kukokuk.domain.rank.service.RankService;
import com.kukokuk.security.SecurityUser;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 랭킹 페이지 뷰 컨트롤러
 */
@Log4j2
@Controller
@RequestMapping("/rank")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    /**
     * 순위 페이지
     *
     * @param securityUser 사용자 정보
     * @param model        speedQuizRanks, levelQuizRanks, dictationQuizRanks
     * @return 순위 페이지
     */
    @GetMapping
    public String rankPage(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        log.info("RankController rankPage() 컨트롤러 실행");

        // 컨텐츠별 데이터 조회하여 model에 담기
        // thread-safety 이슈 방지로 rankRequestDto 객체 간의 독립성이 보장을 위해
        // RankRequestDto 각각 적용
        // 정확한 컨텐츠 타입을 입력 및 enum 메소드 활용하기 위해 ContentTypeEnum 사용
        List<ContentTypeEnum> contentTypes = Arrays.asList(
            ContentTypeEnum.SPEED // sppedQuizRanks에 사용
            , ContentTypeEnum.DICTATION // dictationQuizRanks에 사용
        );

        String rankMonth = DateUtil.getToday("yyyy-MM"); // 당월
        int userNo = securityUser.getUser().getUserNo(); // 사용자 번호

        // contentTypes을 순환하며 model에 담기
        // speedQuizRanks, levelQuizRanks, dictationQuizRanks
        for (ContentTypeEnum type : contentTypes) {
            RankRequestDto rankRequestDto = RankRequestDto.builder()
                .userNo(userNo)
                .rankMonth(rankMonth)
                .limit(PaginationEnum.COMPONENT_ROWS)
                .contentType(type.name()) // String으로 변환
                .build();

            // 속성명 동적으로 구성(컨텐츠 타입 소문자 + QuizRanks)
            model.addAttribute(type.name().toLowerCase() + "QuizRanks",
                rankService.getContentRanksIncludeUserByMonth(rankRequestDto)
            );
        }
        // 레벨 랭킹 조회 (날짜 상관없이)
        model.addAttribute("levelRanks",
            rankService.getLevelRanksIncludeUser(userNo, PaginationEnum.COMPONENT_ROWS)
        );
        return "rank/main";
    }
}