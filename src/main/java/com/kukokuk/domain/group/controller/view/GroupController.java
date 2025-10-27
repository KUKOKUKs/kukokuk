package com.kukokuk.domain.group.controller.view;

import com.kukokuk.common.constant.PaginationEnum;
import com.kukokuk.common.dto.Page;
import com.kukokuk.common.exception.AppException;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
     * @param page 조회할 페이지 번호 (기본값 1)
     * @param keyword 그룹 검색어 (없으면 랜덤 리스트 반환)
     * @param securityUser 로그인 사용자 정보
     * @param model groups, myGroup, myGroupTwentyRooms, myGroupActiveRoomNo
     * @return group/main 템플릿
     */
    @GetMapping
    public String groupPage(
        @RequestParam(defaultValue = "1") int page
        , @RequestParam(required = false) String keyword
        , @AuthenticationPrincipal SecurityUser securityUser
        , Model model) {
        log.info("GroupController groupPage() 컨트롤러 실행");

        // 그룹 검색 관련 로직
        if (keyword == null || keyword.isBlank()) {
            // 검색어 없으면 랜덤 그룹 보여주기
            log.info("검색어 없음: 랜덤 그룹 노출(기본)");
            
            // 템플릿 구조를 통일하기 위해 Page객체에 담기
            Page<Group> randomGroups = new Page<>();
            randomGroups.setItems(groupService.getRandomGroups(PaginationEnum.COMPONENT_ROWS));
            model.addAttribute("groups", randomGroups);
        } else {
            // 검색어 있으면 검색 결과 보여주기
            log.info("검색어 있음: 검색 결과 노출");
            Map<String, Object> condition = new HashMap<>();
            condition.put("keyword", keyword);
            model.addAttribute("groups", groupService.getGroups(page, condition, PaginationEnum.COMPONENT_ROWS));
        }
        
        int userNo = securityUser.getUser().getUserNo(); // 사용자 번호
        Integer myGroupNo = securityUser.getUser().getGroupNo(); // 사용자가 속한 그룹 번호

        Group myGroup = null; // 내가 가입한 그룹
        List<TwentyRoom> myGroupTwentyRooms = Collections.emptyList(); // 스무고개 방 리스트
        Integer myGroupActiveRoomNo = null; // 그룹의 스무고개 방을 생성했을 경우 방 번호
        if (myGroupNo != null) {
            myGroup = groupService.getGroupByUserNo(userNo);
            myGroupTwentyRooms = twentyService.getRecentTodayTwentyRoomListByGroupNo(myGroupNo, 3);
            myGroupActiveRoomNo = twentyService.getRoomNoByRoomList(myGroupTwentyRooms);
        }

        model.addAttribute("myGroup", myGroup);
        model.addAttribute("myGroupTwentyRooms", myGroupTwentyRooms);
        model.addAttribute("myGroupActiveRoomNo", myGroupActiveRoomNo);

        return "group/main";
    }

    /**
     * 교사 그룹 관리 페이지
     * <ul>
     *     <li>groupNo는 null값을 허용하면서 예외 발생되지 않도록 처리</li>
     *     <li>교사 권한 사용자가 생성한 그룹이 없을 경우 model의 전달 값이 전부 null 또는 빈 리스트</li>
     *     <li>생성한 그룹이 없을 경우 groupNo값을 전달 받았더라도 무조건 사용자의 그룹이 아니기 때문</li>
     *     <li>생성한 그룹이 있을 경우 groupNo값으로 사용자가 생성한 그룹인지 확인</li>
     *     <li>groupNo값이 null이더라도 같은 로직 실행</li>
     *     <li>사용자가 생성한 그룹이 아닐 경우 예외 처리하지 않고 그룹 관리 페이지에 기본 그룹(최신순 첫 번째)정보 적용</li>
     *     <li>사용자가 생성한 그룹일 경우 그룹 관리 페이지에 groupNo의 그룹 정보 적용</li>
     * </ul>
     * @param groupNo 교사 그룹 관리 페이지에 적용할 그룹 번호
     * @param securityUser 로그인 사용자 정보
     * @param model currentGroup, teacherGroups, currentGroupTwentyRooms, currentGroupActiveRoomNo
     * @return group/teacher 템플릿
     */
    @GetMapping("/teacher")
    public String teacherGroupPage(
        @RequestParam(value = "groupNo", required = false) Integer groupNo
        , @AuthenticationPrincipal SecurityUser securityUser
        , Model model) {
        log.info("GroupController teacherGroupPage() 컨트롤러 실행");

        int userNo = securityUser.getUser().getUserNo(); // 교사 권한 사용자 번호
        List<Group> teacherGroups = groupService.getTeacherGroups(userNo); // 교사 권한 사용가 생성한 그룹 최신순 정렬

        Group currentGroup = null; // 현재 선택된 그룹
        List<TwentyRoom> currentGroupTwentyRooms = Collections.emptyList(); // 그룹의 스무고개 방 리스트
        Integer currentGroupActiveRoomNo = null; // 그룹의 스무고개 방을 생성했을 경우 방 번호

        // 교사 권한 사용자가 생성한 그룹이 있을 경우
        if (!teacherGroups.isEmpty()) {
            currentGroup = teacherGroups.get(0); // 우선 최신순 첫 번째 데이터 적용

            // 사용자가 생성한 그룹인지 확인(groupNo가 null인 경우 결과값도 null)
            Optional<Group> owned = teacherGroups.stream()
                .filter(group -> Objects.equals(group.getGroupNo(), groupNo))
                .findFirst();

            // groupNo가 교사 권한 사용자의 그룹일 경우
            if (owned.isPresent()) {
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

        // groupNo가 null이고 teacherGroups도 빈 리스트라면 전부 null 또는 빈 리스트 값
        model.addAttribute("currentGroup", currentGroup);
        model.addAttribute("teacherGroups", teacherGroups);
        model.addAttribute("currentGroupTwentyRooms", currentGroupTwentyRooms);
        model.addAttribute("currentGroupActiveRoomNo", currentGroupActiveRoomNo);

        return "group/teacher";
    }

    /**
     * 사용자의 그룹 탈퇴 요청
     * @param groupNo 그룹 번호
     * @param securityUser 사용자 정보
     * @return 그룹 메인 페이지 요청
     */
    @PostMapping("/delete")
    public String groupOut(
        @RequestParam("groupNo") int groupNo
        , @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("GroupController groupOut() 컨트롤러 실행 groupNo: {}", groupNo);

        // 사용자가 속한 그룹인지 확인
        int userNo = securityUser.getUser().getUserNo();
        Group userGroup = groupService.getGroupByUserNo(userNo);
        if (userGroup == null || userGroup.getGroupNo() != groupNo) {
            throw new AppException("해당 그룹에 접근 권한이 없습니다.");
        }

        groupService.deleteGroupUser(userNo, groupNo); // 사용자 그룹 탈퇴 요청

        return "redirect:/group"; // 그룹 페이지 요청
    }

    /**
     * 사용자 그룹 가입 요청
     * @param groupNo 그룹 번호
     * @param securityUser 사용자 번호
     * @return 그룹 메인 페이지 요청
     */
    @PostMapping("/join")
    public String groupJoin(
        @RequestParam("groupNo") int groupNo
        , @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("GroupController groupJoin() 컨트롤러 실행 groupNo: {}", groupNo);
        groupService.insertGroupUser(securityUser.getUser().getUserNo(), groupNo);
        return "redirect:/group"; // 그룹 페이지 요청
    }

}
