package com.kukokuk.domain.twenty.dto;

import com.kukokuk.common.util.FilePathUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * 클라이언트에서 입력된 실시간 데이터를 DB에 담고 다시 반환하기 위한 DTO
 */
@Getter
@Setter
@Alias("SendStdMsg")
public class SendStdMsg {

    private int logNo;
    private int userNo;
    private int roomNo;
    private String content;
    private String type; // 질문인 경우 Q, 정답인 경우 A
    private Integer cnt;
    private String isSuccess; // 정답에 대한 O,X
    private String answer;    // 질문에 대한 O,X
    String nickName;          //유저 닉네임
    String profileFilename;

    // 프로필 이미지 경로 생성
    public String getProfileFileUrl() {
        return FilePathUtil.getProfileImagePath(userNo, profileFilename);
    }
}
