package com.board.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;

/**
 * Security context factory for creating mock user authentication in tests.
 */
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserPrincipal principal = new UserPrincipal(customUser.userId(), customUser.email());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, Collections.emptyList());
        context.setAuthentication(auth);

        return context;
    }
}
