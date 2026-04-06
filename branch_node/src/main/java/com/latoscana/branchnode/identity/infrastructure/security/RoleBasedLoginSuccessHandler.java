package com.latoscana.branchnode.identity.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RoleBasedLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Generate token
        String token = tokenProvider.generateToken(authentication);

        // Add cookie
        Cookie jwtCookie = new Cookie("JWT-TOKEN", token);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(86400); // 24 hours
        response.addCookie(jwtCookie);

        // Determine redirect URL
        String redirectUrl = "/";
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (auth.getAuthority().equals("ROLE_MANAGER")) {
                redirectUrl = "/";
                break;
            } else if (auth.getAuthority().equals("ROLE_CASHIER")) {
                redirectUrl = "/cashier";
                break;
            } else if (auth.getAuthority().equals("ROLE_WAITER")) {
                redirectUrl = "/waiter";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
