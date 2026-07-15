package com.marketplace.professional.auth.dto;

import com.marketplace.professional.domain.enums.Role;

/**
 * Response returned by both {@code POST /auth/register} and {@code POST /auth/login}.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String email,
        String fullName,
        Role role
) {
}
