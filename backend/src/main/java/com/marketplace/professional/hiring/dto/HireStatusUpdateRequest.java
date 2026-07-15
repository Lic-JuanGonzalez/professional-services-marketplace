package com.marketplace.professional.hiring.dto;

import com.marketplace.professional.domain.enums.HireStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Payload for transitioning a hire's status (accept, reject, complete, cancel).
 */
public record HireStatusUpdateRequest(
        @NotNull HireStatus status
) {
}
