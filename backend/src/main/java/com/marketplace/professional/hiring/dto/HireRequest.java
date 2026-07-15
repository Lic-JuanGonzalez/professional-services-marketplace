package com.marketplace.professional.hiring.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Payload for creating a new hire request against a published service offering.
 */
public record HireRequest(
        @NotNull Long serviceOfferingId,
        Instant scheduledDate,
        String notes
) {
}
