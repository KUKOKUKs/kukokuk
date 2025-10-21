package com.kukokuk.domain.group.controller.api;

import com.kukokuk.common.constant.PaginationEnum;
import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.dto.Page;
import com.kukokuk.common.util.ResponseEntityUtils;
import com.kukokuk.domain.group.dto.GroupFormDto;
import com.kukokuk.domain.group.service.GroupService;
import com.kukokuk.domain.group.vo.Group;
import com.kukokuk.security.SecurityUser;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class ApiGroupController {

    private final ModelMapper modelMapper;
    private final GroupService groupService;

    /**
     * 그룹 검색(페이지네이션)
     * <p>
     * keyword 유무에 따라 랜덤리스트(기본값)/검색 리스트 전달
     * @param page 조회할 페이지 번호 (기본값 1)
     * @param keyword 검색어
     * @return 그룹 목록 정보, 페이지네이션(랜덤리스트일 경우 null)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Group>>> searchGroups(
        @RequestParam(defaultValue = "1") int page
        , @RequestParam(required = false) String keyword) {
        log.info("ApiGroupController searchGroups() 컨트롤러 실행 page: {}, keyword: {}", page, keyword);

        if (keyword == null || keyword.isBlank()) {
            // 검색어 없으면 랜덤 그룹 보여주기
            log.info("ApiGroupController getRandomGroups() 랜덤 그룹 목록 조회(기본값)");

            // 템플릿 구조를 통일하기 위해 Page객체에 담기
            Page<Group> randomGroups = new Page<>();
            randomGroups.setItems(groupService.getRandomGroups(PaginationEnum.COMPONENT_ROWS));
            return ResponseEntityUtils.ok(randomGroups);
        } else {
            // 검색어 있으면 검색 결과 보여주기
            log.info("ApiGroupController getGroups() 검색 그룹 목록 조회");
            Map<String, Object> condition = new HashMap<>();
            condition.put("keyword", keyword);
            return ResponseEntityUtils.ok(groupService.getGroups(page, condition));
        }
    }

    /**
     * 그룹 등록
     * @param groupFormDto 그룹 정보
     * @param securityUser 로그인 사용자 정보
     * @param result 유효성 검증 결과
     * @return isValid=처리결과 유효성 검증 실패 정보 또는 등록 성공 시 생성된 groupNo 값
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')") // TEACHER 권한 없으면 403
    public ResponseEntity<ApiResponse<Map<String, Object>>> createGroup(
        @Valid @ModelAttribute GroupFormDto groupFormDto // FormData 매핑
        , BindingResult result
        , @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("ApiGroupController createGroup() 컨트롤러 실행");

        if (result.hasErrors()) {
            // 유효성 검증 실패 시
            log.info("ApiGroupController createGroup() 유효성 검증 실패: {}", result.getAllErrors());
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    fe -> Objects.requireNonNullElse(fe.getDefaultMessage(), "")
                ));

            Map<String, Object> data = new HashMap<>();
            data.put("isValid", false);
            data.put("errors", errors);

            return ResponseEntityUtils.ok("입력값 검증 실패", data);
        }

        Group group = modelMapper.map(groupFormDto, Group.class);
        int teacherNo = securityUser.getUser().getUserNo();
        int groupNo = groupService.insertGroup(teacherNo, group); // 생성된 pk 반환

        Map<String, Object> data = new HashMap<>();
        data.put("isValid", true);
        data.put("groupNo", groupNo);

        return ResponseEntityUtils.ok("그룹 생성 성공", data);
    }

    /**
     * 그룹 수정
     * @param groupFormDto 그룹 정보
     * @param securityUser 로그인 사용자 정보
     * @param result 유효성 검증 결과
     * @return isValid=처리결과 유효성 검증 실패 정보
     */
    @PutMapping
    @PreAuthorize("hasRole('TEACHER')") // TEACHER 권한 없으면 403
    public ResponseEntity<ApiResponse<Map<String, Object>>> modifyGroup(
        @Valid @ModelAttribute GroupFormDto groupFormDto // FormData 매핑
        , BindingResult result
        , @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("ApiGroupController modifyGroup() 컨트롤러 실행");

        if (result.hasErrors()) {
            // 유효성 검증 실패 시
            log.info("ApiGroupController modifyGroup() 유효성 검증 실패: {}", result.getAllErrors());
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    fe -> Objects.requireNonNullElse(fe.getDefaultMessage(), "")
                ));

            Map<String, Object> data = new HashMap<>();
            data.put("isValid", false);
            data.put("errors", errors);

            return ResponseEntityUtils.ok("입력값 검증 실패", data);
        }

        // 해당 그룹 소유자 확인
        Group group = groupService.getGroupByGroupNo(groupFormDto.getGroupNo());
        int teacherNo = securityUser.getUser().getUserNo();

        // 사용자 소유의 그룹이 아닐 경우
        if (group.getTeacher().getUserNo() != teacherNo) {
            throw new AccessDeniedException("내가 만든 우리반만 수정할 수 있습니다.");
        }

        // 수정 정보 대입
        group.setTitle(groupFormDto.getTitle());
        group.setMotto(groupFormDto.getMotto());

        // 비밀번호를 입력하였을 경우 대입(입력하지 않았을 경우 기존 비밀번호 유지)
        if (!"".equals(groupFormDto.getPassword())) {
            group.setPassword(groupFormDto.getPassword());
        }

        // 비밀번호 삭제 여부(삭제 체크 시 기존 비밀번호 제거하여 공개방으로 설정)
        if (groupFormDto.isDeletePassword()) {
            group.setPassword(null);
        }

        // 그룹 정보 업데이트 요청
        groupService.updateGroup(group);

        Map<String, Object> data = new HashMap<>();
        data.put("isValid", true);
        data.put("groupNo", groupFormDto.getGroupNo());

        return ResponseEntityUtils.ok("그룹 수정 성공", data);
    }

    /**
     * 그룹 삭제
     * <p>
     *     삭제 동의 확인 후
     *     사용자가 소유한 그룹인지 확인하여 삭제처리
     * @param groupNo 삭제할 그룹 번호
     * @param securityUser 인증된 사용자 정보
     * @return 삭제 처리 결과와 실패 시 메세지
     */
    @DeleteMapping("/{groupNo}")
    @PreAuthorize("hasRole('TEACHER')") // TEACHER 권한 없으면 403
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteGroup(
        @PathVariable("groupNo") int groupNo
        , @RequestParam(defaultValue = "false") boolean isDeleteCheck
        , @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("ApiGroupController deleteGroup() 컨트롤러 실행 groupNo: {}, isDeleteCheck: {}", groupNo, isDeleteCheck);

        Map<String, Object> data = new HashMap<>();

        // 삭제 동의 여부 재검증
        if (!isDeleteCheck) {
            data.put("isSuccess", false);
            data.put("message", "우리반 삭제는 동의가 필요합니다.\n삭제를 원하실 경우 '예'를 선택해 주세요.");
            return ResponseEntityUtils.ok("삭제 요청 취소", data);
        }

        // 해당 그룹이 사용자가 소유한 그룹인지 확인
        Group savedGruop = groupService.getGroupByGroupNo(groupNo); // 해당 그룹 정보 가져오기
        if (savedGruop == null || savedGruop.getTeacher().getUserNo() != securityUser.getUser().getUserNo()) {
            throw new AccessDeniedException("찾을 수 없는 반이거나 삭제 권한이 없는 반입니다.");
        }

        // 삭제 요청
        groupService.deleteGroup(savedGruop);

        data.put("isSuccess", true);
        return ResponseEntityUtils.ok("삭제 처리 완료", data);
    }

}
