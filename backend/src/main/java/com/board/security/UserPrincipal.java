package com.board.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.security.Principal;

/**
 * Represents the authenticated user principal in the SecurityContext.
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements Principal {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String email;

    @Override
    public String getName() {
        return email;
    }
}
