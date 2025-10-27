package com.kukokuk.domain.group.mapper;

import com.kukokuk.domain.group.dto.GruopUsersDto;
import com.kukokuk.domain.group.vo.Group;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GroupMapper {

    /**
     * 사용자의 그룹 탈퇴
     * @param userNo 사용자 번호
     * @param groupNo 그룹 번호
     */
    void deleteGroupUser(@Param("userNo") int userNo, @Param("groupNo") int groupNo);

    /**
     * 사용자의 그룹 가입
     * @param userNo 사용자 번호
     * @param groupNo 그룹 번호
     */
    void insertGroupUser(@Param("userNo") int userNo, @Param("groupNo") int groupNo);

    /**
     * 그룹 정보를 전달받아 그룹 삭제
     * @param group 삭제할 그룹 정보
     */
    void deleteGroup(Group group);

    /**
     * 그룹 정보를 전달받아 그룹 수정
     * @param group 수정할 그룹 정보
     */
    void updateGroup(Group group);

    /**
     * 그룹 정보를 전달받아 그룹 등록
     * @param teacherNo 교사 권한 사용자 번호
     * @param group 그룹 정보
     */
    void insertGroup(@Param("teacherNo") int teacherNo
        , @Param("group") Group group);

    /**
     * 그룹 번호로 그룹 정보와 그룹에 속한 사용자 정보 목록 조회
     * @param gruopNo 그룹 번호
     * @return 그룹 정보, 그룹에 속한 사용자 정보 목록
     */
    GruopUsersDto getGruopUsersByGruopNo(int gruopNo);

    /**
     * 그룹 번호로 그룹 정보 조회
     * @param groupNo 그룹 번호
     * @return 그룹 정보
     */
    Group getGroupByGroupNo(int groupNo);

    /**
     * 사용자가 속한 그룹 정보 조회
     * @param userNo 사용자 번호
     * @return 사용자가 속한 그룹 정보
     */
    Group getGroupByUserNo(int userNo);

    /**
     * 사용자 번호로 그룹 목록 정보 조회
     * @param teacherNo 사용자 번호(교사권한)
     * @return 그룹 목록 정보(최신순)
     */
    List<Group> getTeacherGroups(int teacherNo);

    /**
     * 조건에 맞는 그룹 목록 조회
     * @param condition 조회할 데이터의 조건 값들이 담겨 있는 Map 객체
     * @return 그룹 목록 정보
     */
    List<Group> getGroups(Map<String, Object> condition);

    /**
     * 조회할 데이터의 총 행의 수 조회
     * @param condition 조회할 데이터의 조건 값들이 담겨 있는 Map 객체
     * @return 조회할 데이터의 총 데이터 행의 수
     */
    int getTotalRows(Map<String, Object> condition);

    /**
     * 전달 받은 조회할 행의 개수로 최초 랜더링 시 기본 그룹 리스트 조회(임의의 행 조회)
     * (성능 최적화로 최신 1000건 제한으로 1000건 내에서 랜덤으로 조회)
     * @param groupCount 조회할 행의 개수
     * @return 그룹 목록 정보
     */
    List<Group> getRandomGroups(int groupCount);

}
