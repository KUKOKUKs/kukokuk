package com.kukokuk.domain.user.mapper;

import com.kukokuk.domain.user.vo.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * 전달받은 힌트 개수를 사용자 힌트 개수에서 차감하여 업데이트
     * @param hintCount 추가할 힌트 개수
     * @param userNo 사용자 번호
     */
    void updateUserHintCountMinus(@Param("hintCount") int hintCount
        , @Param("userNo") int userNo);

    /**
     * 전달받은 힌트 개수를 사용자 힌트 개수에 더하여 업데이트
     * @param hintCount 추가할 힌트 개수
     * @param userNo 사용자 번호
     */
    void updateUserHintCountPlus(@Param("hintCount") int hintCount
        , @Param("userNo") int userNo);

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
     * 이메일 중복 여부
     * @param usernam 이메일
     * @return 1=중복 / 0 = 중복X
     */
    int isDuplicatedByUsername(String username);
    
    /**
     * 닉네임 중복 여부
     * @param nickname 닉네임
     * @return 1=중복 / 0 = 중복X
     */
    int isDuplicatedByNickname(String nickname);

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
