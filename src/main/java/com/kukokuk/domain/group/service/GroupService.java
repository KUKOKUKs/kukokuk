package com.kukokuk.domain.group.service;

import com.kukokuk.domain.group.mapper.GroupMapper;
import com.kukokuk.domain.group.vo.Group;
import java.util.List;
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
     * 전달 받은 조회할 행의 개수로 최초 랜더링 시 기본 그룹 리스트 조회(임의의 행 조회)
     * (성능 최적화로 최신 1000건 제한으로 1000건 내에서 랜덤으로 조회)
     * @param groupCount 조회할 행의 개수
     * @return 그룹 목록 정보
     */
    public List<Group> getRandomGroups(int groupCount) {
        return groupMapper.getRandomGroups(groupCount);
    }

}
