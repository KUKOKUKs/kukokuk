package com.kukokuk.domain.group.service;

import com.kukokuk.common.dto.Page;
import com.kukokuk.common.dto.Pagination;
import com.kukokuk.domain.group.mapper.GroupMapper;
import com.kukokuk.domain.group.vo.Group;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor // 초기화 되지않은 final 필드나, @NonNull 이 붙은 필드에 대해 생성자를 생성
public class GroupService {

    private final ModelMapper modelMapper;
    private final GroupMapper groupMapper;

    /**
     * 전달 받은 페이지, 조회 조건에 해당하는 그룹 목록 조회
     * @param page 조회할 페이지
     * @param condition 조회할 데이터의 조건 값들이 담겨 있는 Map 객체
     * @return 페이지네이션, 그룹 목록 정보
     */
    public Page<Group> getGroups(int page, Map<String, Object> condition) {
        Page<Group> groupPage = new Page<>(); // 그룹 목록을 담을 Page 객체 생성
        int totalRows = groupMapper.getTotalRows(condition); // 조건에 맞는 모든 데이터 행의 수
        Pagination pagination = new Pagination(page, totalRows); // 페이지네이션 객체 생성

        // 페이징 처리 조건
        condition.put("offset", pagination.getOffset());
        condition.put("rows", pagination.getRows());

        // 조건에 해당하는 그룹 조회 요청
        List<Group> groups = groupMapper.getGroups(condition);

        // 페이지네이션 데이터 목록 세팅
        groupPage.setCondition(condition);
        groupPage.setItems(groups);
        groupPage.setPagination(pagination);

        return groupPage;
    }

    /**
     * 전달 받은 조회할 행의 개수로 최초 랜더링 시 기본 그룹 리스트 조회(임의의 행 조회)
     * (성능 최적화로 최신 1000건 제한으로 1000건 내에서 랜덤으로 조회)
     * @param groupCount 조회할 행의 개수
     * @return 그룹 목록 정보
     */
    public List<Group> getRandomGroups(int groupCount) {
        return groupMapper.getRandomGroups(groupCount);
    }

}
