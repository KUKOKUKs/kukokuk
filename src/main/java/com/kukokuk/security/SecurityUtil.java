package com.kukokuk.security;

import com.kukokuk.vo.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    /**
     * 현재 로그인 사용자의 Authentication을 갱신
     * @param updatedUser DB에서 갱신된 사용자 정보
     */
    public static void updateAuthentication(User updatedUser) {
        Authentication oldAuth =  SecurityContextHolder.getContext().getAuthentication();

        // 새로운 Principal(SecurityUser) 생성
        SecurityUser newPrincipal = new SecurityUser(updatedUser);

        // 새로운 Authentication 객체 생성
        UsernamePasswordAuthenticationToken newAuth =
            new UsernamePasswordAuthenticationToken(
                newPrincipal,
                oldAuth.getCredentials(),
                newPrincipal.getAuthorities()
            );

        // SecurityContext 갱신
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

}
