package com.kukokuk.domain.group.controller.view;

import com.kukokuk.common.constant.PaginationEnum;
import com.kukokuk.common.dto.Page;
import com.kukokuk.domain.group.service.GroupService;
import com.kukokuk.domain.group.vo.Group;
import com.kukokuk.domain.twenty.service.TwentyService;
import com.kukokuk.domain.twenty.vo.TwentyRoom;
import com.kukokuk.security.SecurityUser;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;
    private final TwentyService twentyService;

    /**
     * 그룹 메인 페이지
     * <p>
     * - 검색어(keyword) 유무에 따라 랜덤 그룹 리스트(기본) 또는 검색 결과를 반환
     * - 로그인한 사용자의 권한(교사 / 일반 사용자)에 따라 추가 로직이 수행
     *   <ul>
     *     <li>일반 사용자: SecurityUser에 저장된 groupNo로 가입 그룹 정보를 조회하여 전달</li>
     *     <li>교사:
     *       <ul>
     *         <li>groupNo 파라미터가 없을 경우 → 소유 그룹 중 최신 그룹을 선택(fallback)</li>
     *         <li>groupNo 파라미터가 있을 경우 → 소유 그룹인지 검증 후 현재 그룹으로 선택</li>
     *         <li>선택된 그룹(currentGroup)을 제외한 나머지 소유 그룹 리스트(teacherGroups)도 함께 전달</li>
     *       </ul>
     *     </li>
     *   </ul>
     * - 접근 권한이 없는 경우 Flash 메시지(error)를 전달하고 /group/main 으로 리다이렉트
     *
     * @param page 조회할 페이지 번호 (기본값 1)
     * @param keyword 그룹 검색어 (없으면 랜덤 리스트 반환)
     * @param groupNo 교사 전용: 현재 선택할 그룹 번호 (nullable)
     * @param securityUser 로그인 사용자 정보
     * @param redirectAttributes redirect 시 Flash 메시지 전달
     * @param model 뷰에 전달할 데이터 (groups, myGroup, currentGroup, teacherGroups)
     * @return group/main 템플릿
     */
    @GetMapping
    public String groupPage(
        @RequestParam(defaultValue = "1") int page
        , @RequestParam(required = false) String keyword
        , @RequestParam(required = false) Integer groupNo
        , @AuthenticationPrincipal SecurityUser securityUser
        , RedirectAttributes redirectAttributes
        , Model model) {
        log.info("GroupController groupPage() 컨트롤러 실행");

        // 그룹 검색 관련 로직
        if (keyword == null || keyword.isBlank()) {
            // 검색어 없으면 랜덤 그룹 보여주기
            log.info("검색어 없음: 랜덤 그룹 노출(기본)");
            
            // 템플릿 구조를 통일하기 위해 Page객체에 담기
            Page<Group> randomGroups = new Page<>();
            randomGroups.setItems(groupService.getRandomGroups(PaginationEnum.DEFAULT_ROWS));
            model.addAttribute("groups", randomGroups);
        } else {
            // 검색어 있으면 검색 결과 보여주기
            log.info("검색어 있음: 검색 결과 노출");
            Map<String, Object> condition = new HashMap<>();
            condition.put("keyword", keyword);
            model.addAttribute("groups", groupService.getGroups(page, condition));
        }
        
        // 로그인 사용자의 권한 확인
        boolean isTeacher = securityUser.getUser().getRoleNames().contains("ROLE_TEACHER");
        int userNo = securityUser.getUser().getUserNo();
        Integer memberGroupNo = securityUser.getUser().getGroupNo();

        // 내가 가입한 그룹, 그룹의 스무고개 방 리스트 최근 3건(일반/교사 공통 로직)
        Group myGroup = null;
        List<TwentyRoom> myGroupTwentyRooms = Collections.emptyList();
        Integer myGroupActiveRoomNo = null; // 그룹의 스무고개 방을 생성했을 경우 방 번호
        if (memberGroupNo != null) {
            myGroup = groupService.getGroupByGroupNo(memberGroupNo);
            myGroupTwentyRooms = twentyService.getRecentTodayTwentyRoomListByGroupNo(memberGroupNo, 3);
            myGroupActiveRoomNo = twentyService.getRoomNoByRoomList(myGroupTwentyRooms);
        }
        model.addAttribute("myGroup", myGroup);
        model.addAttribute("myGroupTwentyRooms", myGroupTwentyRooms);
        model.addAttribute("myGroupActiveRoomNo", myGroupActiveRoomNo);

        // 교사 권한 전용 로직
        List<Group> teacherGroups = Collections.emptyList(); // 교사가 소유한 그룹

        // 현재 선택된 그룹, 그룹의 스무고개 방 리스트 최근 3건
        Group currentGroup = null;
        List<TwentyRoom> currentGroupTwentyRooms = Collections.emptyList();
        Integer currentGroupActiveRoomNo = null; // 그룹의 스무고개 방을 생성했을 경우 방 번호

        if (isTeacher) {
            teacherGroups = groupService.getTeacherGroups(userNo); // 최신순 정렬

            if (teacherGroups.isEmpty()) {
                if (groupNo != null) {
                    // 소유 그룹이 없는 경우 groupNo가 있을 경우 접근 권한 없음
                    redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
                    return "redirect:/group/main";
                }
            } else {
                if (groupNo == null) {
                    // groupNo가 없을 경우 최신 그룹 fallback
                    currentGroup = teacherGroups.get(0);
                } else {
                    // groupNo가 있을 경우 소유 그룹인지 확인
                    Optional<Group> owned = teacherGroups.stream()
                        .filter(group -> Objects.equals(group.getGroupNo(), groupNo))
                        .findFirst();

                    if (owned.isEmpty()) {
                        // 소유한 그룹이 아닌 경우
                        redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
                        return "redirect:/group/main";
                    }

                    // 소유한 그룹이 있다면
                    currentGroup = owned.get();
                }

                // teacherGroups에서 currentGroup 제거(사용하기 적합하게 가공)
                int currentGroupNo = currentGroup.getGroupNo();
                teacherGroups = teacherGroups.stream()
                    .filter(group -> !Objects.equals(group.getGroupNo(), currentGroupNo))
                    .toList();
                currentGroupTwentyRooms = twentyService.getRecentTodayTwentyRoomListByGroupNo(currentGroupNo, 3);
                currentGroupActiveRoomNo = twentyService.getRoomNoByRoomList(currentGroupTwentyRooms);
            }
        } else {
            // 일반 사용자면서 groupNo가 넘어온 경우(잘못된 접근)
            if (groupNo != null) {
                redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
                return "redirect:/group/main";
            }
        }

        model.addAttribute("currentGroup", currentGroup);
        model.addAttribute("teacherGroups", teacherGroups);
        model.addAttribute("currentGroupTwentyRooms", currentGroupTwentyRooms);
        model.addAttribute("currentGroupActiveRoomNo", currentGroupActiveRoomNo);

        return "group/main";
    }

}
