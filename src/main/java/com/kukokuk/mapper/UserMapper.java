package com.kukokuk.mapper;

import com.kukokuk.vo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * 신규 사용자 등록
     * @param user 신규 사용자 정보
     */
    void insertUser(User user);

    /**
     * 사용자 권한 등록
     * @param userNo 사용자 번호
     * @param roleName 권한 이름
     */
    void insertUserRole(@Param(("userNo")) int userNo
        , @Param("roleName") String roleName);

    /**
     * 사용자 이메일로 사용자 정보 조회
     * @param username 사용자 이메일
     * @return 사용자 정보
     */
    User getUserByUsername(String username);

    /**
     * 사용자 닉네임으로 사용자 정보 조회
     * @param nickname 사용자 닉네임
     * @return 사용자 정보
     */
    User getUserByNickname(String nickname);

}
