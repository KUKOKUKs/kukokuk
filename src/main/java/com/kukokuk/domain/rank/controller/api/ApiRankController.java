package com.kukokuk.domain.rank.controller.api;

import com.kukokuk.common.constant.ContentTypeEnum;
import com.kukokuk.common.constant.PaginationEnum;
import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.util.DateUtil;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.rank.dto.RankRequestDto;
import com.kukokuk.domain.rank.dto.RanksResponseDto;
import com.kukokuk.domain.rank.service.RankService;
import com.kukokuk.security.SecurityUser;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/ranks")
@RequiredArgsConstructor
public class ApiRankController {

    private final RankService rankService;

    /**
     * 사용자 랭크를 포함한 랭크 목록 조회
     * <p>
     *     groupNo 입력 여부에 따라 그룹/일반 컨텐츠별 랭크 목록 조회 요청
     * @param rankMonth 랭크를 조회할 월
     * @param groupNo 그룹 번호
     * @param securityUser 사용자 정보
     * @return 랭크 목록 정보(userRank 정렬)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, RanksResponseDto>>> contentRanks(
         @RequestParam(required = false) String rankMonth
        , @RequestParam(required = false) Integer groupNo
        , @AuthenticationPrincipal SecurityUser securityUser) {
        log.info(
            "ApiRankingController contentRanks() 컨트롤러 실행 rankMonth: {}"
            , rankMonth
        );

        // 전달 받은 rankMonth 값이 없을 경우 당월
        rankMonth = (rankMonth == null || rankMonth.isBlank())
            ? DateUtil.getToday("yyyy-MM")
            : rankMonth;

        // Enum으로 명확한 타입 정의
        List<ContentTypeEnum> contentTypes = Arrays.asList(
            ContentTypeEnum.SPEED // sppedQuizRanks에 사용
            , ContentTypeEnum.DICTATION // dictationQuizRanks에 사용
        );

        int userNo = securityUser.getUser().getUserNo(); // 사용자 번호

        // 반환할 데이터를 담을 맵
        Map<String, RanksResponseDto> ranksResponseDtos = new HashMap<>();

        // contentTypes을 순환하며 Map에 추가
        // speedQuizRanks, levelQuizRanks, dictationQuizRanks
        for (ContentTypeEnum type : contentTypes) {
            RankRequestDto rankRequestDto = RankRequestDto.builder()
                .userNo(userNo)
                .rankMonth(rankMonth)
                .limit(PaginationEnum.COMPONENT_ROWS)
                .contentType(type.name()) // String으로 변환
                .groupNo(groupNo) // groupNo가 있을 경우 그룹 랭크 조회
                .build();

            // 키명 동적으로 구성(컨텐츠 타입 소문자 + QuizRanks)
            ranksResponseDtos.put(
                type.name().toLowerCase() + "QuizRanks"
                , rankService.getContentRanksIncludeUserByMonth(rankRequestDto)
            );
        }

        // 사용자 랭크 포함 랭크 목록 정보(groupNo 여부에 따라 그룹/일반 컨텐츠별 랭크 목록)
        return ResponseEntityUtils.ok(ranksResponseDtos);
    }

}