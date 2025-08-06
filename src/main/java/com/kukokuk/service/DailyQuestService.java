package com.kukokuk.service;

import com.kukokuk.exception.AppException;
import com.kukokuk.mapper.DailyQuestUserMapper;
import com.kukokuk.vo.DailyQuestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyQuestService {

    private final DailyQuestUserMapper dailyQuestUserMapper;

    /**
     * 해당 일일도전과제 수행 정보의 IS_OBTAINED 컬럼을 "Y"로 변경 - 로그인한 사용자와 해당 도일일도전과제 수행 정본의 소유자가 일치하는지 확인 - 이미
     * "Y"인 경우 예외 처리
     *
     * @param dailyQuestUserNo 일일도전과제 수행 정보 식별자
     * @param userNo           현재 로그인한 사용자 식별자
     */
    public void updateDailyQuestUser(int dailyQuestUserNo, int userNo) {

        // 일일도전과제 수행 정보 식별자로 정보 조회
        DailyQuestUser dailyQuestUser = dailyQuestUserMapper.getDailyQuestUserByNo(
            dailyQuestUserNo);

        // 로그인한 사용자와 해당 도일일도전과제 수행 정본의 소유자가 일치하는지 확인
        if (dailyQuestUser.getUserNo() != userNo) {
            // 인가 실패이므로 시큐리티가 처리할 수 있도록 AccessDeniedException 발생
            throw new AccessDeniedException("해당 도전과제에 대한 권한이 없습니다.");
        }

        // 이미 IS_OBTAINED가 "Y"인 경우 처리
        if ("Y".equals(dailyQuestUser.getIsObtained())) {
            // RestControllerAdvice에서 AppException을 처리해서 에러 JSON응답 전달
            throw new AppException("이미 획득한 아이템입니다");
        }

        // 해당 일일도전과제 수행 정보의 IS_OBTAINED 컬럼을 "Y"로 변경
        dailyQuestUserMapper.updateIsObtained(dailyQuestUserNo);
    }
}
