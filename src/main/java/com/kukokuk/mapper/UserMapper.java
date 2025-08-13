package com.kukokuk.mapper;

import com.kukokuk.vo.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * 사용자 경험치 추가 및 레벨업
     * @param user 사용자 정보
     */
    void updateUserExperienceAndLevelUp(User user);

    /**
     * 사용자 힌드 개수 -1 업데이트
     * @param userNo 사용자 번호
     */
    void updateUserHintCountMinus(int userNo);

    /**
     * 사용자 힌드 개수 +1 업데이트
     * @param userNo 사용자 번호
     */
    void updateUserHintCountPlus(int userNo);

    /**
     * 사용자 프로필 이미지 정보 업데이트
     * @param profileFilename 프로필 이미지 파일명
     * @param userNo 사용자 번호
     */
    void updateUserProfileImage(@Param("userNo") int userNo
        , @Param("profileFilename") String profileFilename);

    /**
     * 사용자 정보 업데이트
     * @param user 사용자 정보
     */
    void updateUser(User user);

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
    void insertUserRole(@Param("userNo") int userNo
        , @Param("roleName") String roleName);

    /**
     * 사용자 번호로 사용자, 사용자 권한 정보 조회
     * @param userNo 사용자 번호
     * @return 사용자, 사용자 권한 정보
     */
    User getUserByUserNoWithRoleNames(int userNo);

    /**
     * username으로 사용자, 사용자 권한 정보 조회
     * @param username username
     * @return 사용자, 사용자 권한 정보
     */
    User getUserByUsernameWithRoleNames(String username);

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

    /**
     * 사용자 번호로 사용자 정보 조회
     * @param userNo 사용자 번호
     * @return 사용자 정보
     */
    User getUserByUserNo(int userNo);

    /**
     * 사용자 번호로 사용자 권한 정보 조회
     * @param userNo 사용자 번호
     * @return 사용자 권한 목록
     */
    List<String> getUserRoleNamesByUserNo(int userNo);

}
