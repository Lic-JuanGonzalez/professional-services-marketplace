package com.marketplace.professional.auth.dto;

import com.marketplace.professional.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload for {@code POST /auth/register}.
 * Only {@link Role#CLIENT} and {@link Role#PROFESSIONAL} are accepted;
 * {@link Role#ADMIN} registration is rejected by {@code AuthService}.
 */
public record RegisterRequest(

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @NotNull(message = "Role is required")
        Role role
) {
}
