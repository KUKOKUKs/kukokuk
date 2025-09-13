package com.kukokuk.domain.group.mapper;

import com.kukokuk.domain.group.vo.Group;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMapper {

    /**
     * 전달 받은 조회할 행의 개수로 최초 랜더링 시 기본 그룹 리스트 조회(임의의 행 조회)
     * (성능 최적화로 최신 1000건 제한으로 1000건 내에서 랜덤으로 조회)
     * @param groupCount 조회할 행의 개수
     * @return 그룹 목록 정보
     */
    List<Group> getRandomGroups(int groupCount);

}
