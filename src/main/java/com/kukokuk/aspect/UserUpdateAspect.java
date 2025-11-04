package com.kukokuk.aspect;

import com.kukokuk.domain.user.service.UserService;
import com.kukokuk.domain.user.vo.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Log4j2
@Aspect
@Component
@RequiredArgsConstructor
public class UserUpdateAspect {

    private final UserService userService;

    @AfterReturning(
        pointcut = "execution(* com.kukokuk.domain.user.service.UserService.update*(..))"
            + "|| execution(* com.kukokuk.domain.user.service.UserService.minus*(..))"
            + "|| execution(* com.kukokuk.domain.user.service.UserService.plus*(..))"
            + "|| execution(* com.kukokuk.domain.user.service.UserService.delete*(..))"
            + "|| execution(* com.kukokuk.domain.group.service.GroupService.insertGroupUser(..))"
            + "|| execution(* com.kukokuk.domain.group.service.GroupService.deleteGroupUser(..))"
    )
    public void refreshAuthenticationAdvice() {
        log.info("UserUpdateAspect refreshAuthenticationAdvice() 실행 Authentication 갱신");

        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            log.info("현재 로그인 사용자: {} (userNo: {})", currentUser.getUsername(), currentUser.getUserNo());
            userService.refreshAuthentication(currentUser.getUserNo());
            log.info("Authentication 갱신 완료");
        } else {
            log.info("로그인 사용자 없음. 갱신하지 않음");
        }
    }
}
